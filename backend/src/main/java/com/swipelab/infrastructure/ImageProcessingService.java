package com.swipelab.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Handles all image processing for species reference images:
 * <ul>
 *   <li>File-type validation (JPEG / PNG / WEBP / GIF / BMP)</li>
 *   <li>Resizing: max 1920px on longest edge, preserving aspect ratio</li>
 *   <li>Compression: JPEG quality 0.80</li>
 *   <li>Thumbnail generation: max 200px on longest edge</li>
 * </ul>
 *
 * Uses only Java 21's built-in {@code javax.imageio} — no extra Maven dependency.
 */
@Slf4j
@Service
public class ImageProcessingService {

    private static final int MAX_FULL_PX      = 1920;
    private static final int MAX_THUMB_PX     = 200;
    private static final float JPEG_QUALITY   = 0.80f;
    private static final long MAX_BYTES       = 5L * 1024 * 1024; // 5 MB pre-compression gate

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png",
            "image/webp", "image/gif", "image/bmp"
    );

    /** Output record: both paths + compressed file size. */
    public record ProcessedImageResult(
            String imagePath,       // /uploads/ref/<uuid>.jpg
            String thumbnailPath,   // /uploads/ref/thumb/<uuid>.jpg
            long fileSizeBytes
    ) {}

    private final Path refDir;
    private final Path thumbDir;

    public ImageProcessingService() {
        this.refDir   = Paths.get("uploads", "ref").toAbsolutePath().normalize();
        this.thumbDir = refDir.resolve("thumb");
        try {
            Files.createDirectories(refDir);
            Files.createDirectories(thumbDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create reference-image directories", ex);
        }
    }

    /**
     * Validates, compresses, and generates a thumbnail for an uploaded image file.
     *
     * @param file the raw multipart upload
     * @return paths to the compressed full image and its thumbnail
     * @throws IllegalArgumentException for unsupported type or size violations
     * @throws IOException              on disk I/O failure
     */
    public ProcessedImageResult processAndStore(MultipartFile file) throws IOException {
        validateMimeType(file);
        validateFileSize(file);

        BufferedImage original = readImage(file);

        // Compress + resize full image
        BufferedImage compressed = resizeIfNeeded(original, MAX_FULL_PX);
        String uuid      = UUID.randomUUID().toString();
        Path fullPath    = refDir.resolve(uuid + ".jpg");
        writeJpeg(compressed, fullPath, JPEG_QUALITY);

        // Generate thumbnail from the already-resized image
        BufferedImage thumb = resizeIfNeeded(compressed, MAX_THUMB_PX);
        Path thumbPath  = thumbDir.resolve(uuid + ".jpg");
        writeJpeg(thumb, thumbPath, JPEG_QUALITY);

        long sizeBytes = Files.size(fullPath);
        log.info("Processed reference image → full={} thumb={} size={}B", fullPath, thumbPath, sizeBytes);

        return new ProcessedImageResult(
                "/uploads/ref/"        + uuid + ".jpg",
                "/uploads/ref/thumb/"  + uuid + ".jpg",
                sizeBytes
        );
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported image type: " + contentType +
                    ". Accepted: jpeg, png, webp, gif, bmp.");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "File too large: " + (file.getSize() / 1024 / 1024) + "MB. Max: 5MB.");
        }
    }

    private BufferedImage readImage(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IllegalArgumentException("Cannot decode image — unsupported or corrupt format.");
            }
            // Normalise to RGB — avoids JPEG writer failing on ARGB/indexed images
            if (img.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgb.createGraphics();
                g.drawImage(img, 0, 0, Color.WHITE, null);
                g.dispose();
                return rgb;
            }
            return img;
        }
    }

    /**
     * Returns a scaled copy of {@code src} if its longest edge exceeds {@code maxPx}.
     * If already within limits, returns the original instance unchanged.
     */
    private BufferedImage resizeIfNeeded(BufferedImage src, int maxPx) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (Math.max(w, h) <= maxPx) {
            return src;
        }
        double scale = (double) maxPx / Math.max(w, h);
        int newW = (int) Math.round(w * scale);
        int newH = (int) Math.round(h * scale);

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return out;
    }

    /** Writes a BufferedImage as JPEG to {@code dest} at the given quality (0.0–1.0). */
    private void writeJpeg(BufferedImage img, Path dest, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG ImageWriter found on this JVM");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (FileImageOutputStream out = new FileImageOutputStream(dest.toFile())) {
            writer.setOutput(out);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
