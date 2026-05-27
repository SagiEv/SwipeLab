import { Platform } from 'react-native';

/**
 * Handles saving a CSV blob per platform:
 * - Web: triggers native browser "Save As" dialog via temporary <a> element
 * - Mobile: writes to device storage and opens the share sheet
 */
export async function downloadCsvBlob(blob: Blob, filename: string): Promise<void> {
  if (Platform.OS === 'web') {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  } else {
    const FileSystem = require('expo-file-system');
    const Sharing = require('expo-sharing');

    const text = await blob.text();
    const fileUri = FileSystem.documentDirectory + filename;
    await FileSystem.writeAsStringAsync(fileUri, text, {
      encoding: FileSystem.EncodingType.UTF8,
    });
    await Sharing.shareAsync(fileUri, {
      mimeType: 'text/csv',
      UTI: 'public.comma-separated-values-text',
    });
  }
}
