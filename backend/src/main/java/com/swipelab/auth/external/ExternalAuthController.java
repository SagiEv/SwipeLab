package com.swipelab.auth.external;

import com.swipelab.auth.domain.AuthMapper;
import com.swipelab.dto.request.ExternalLoginRequest;
import com.swipelab.dto.response.UserProfileResponse;
import com.swipelab.users.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/external")
@RequiredArgsConstructor
public class ExternalAuthController {

    private final StardbiAuthService stardbiAuthService;
    private final AuthMapper authMapper;

    /**
     * Called by the frontend immediately after a successful Stardbi login.
     * Validates the Stardbi access token, auto-provisions a local SwipeLab
     * user if this is their first time, and returns the user profile.
     *
     * Expected request body: the full Stardbi login response object.
     */
    @PostMapping("/stardbi/loginExternal")
    public ResponseEntity<UserProfileResponse> loginExternal(
            @Valid @RequestBody ExternalLoginRequest request) {

        User user = stardbiAuthService.loginExternal(request);
        if (user != null) {
            UserProfileResponse profile = authMapper.toUserProfileResponse(user);
            return ResponseEntity.ok(profile);
        }
        return ResponseEntity.status(401).build();
    }
}
