package com.waregang.api_gateway.users.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    @GetMapping("/api/auth/profile")
    public ResponseEntity<UserProfileDto> profile(
            @AuthenticationPrincipal OidcUser principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserProfileDto profile = new UserProfileDto(
                principal.getSubject(),
                principal.getEmail(),
                principal.getClaimAsString("nickname"),
                principal.getClaimAsString("roles"),
                principal.getClaimAsString("warehouseId")
        );

        return ResponseEntity.ok(profile);
    }
}