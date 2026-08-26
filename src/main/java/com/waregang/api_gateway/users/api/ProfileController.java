package com.waregang.api_gateway.users.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ProfileController {

    @GetMapping("/api/auth/profile")
    public Mono<ResponseEntity<UserProfileDto>> profile(
            @AuthenticationPrincipal OidcUser principal
    ) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        UserProfileDto profile = new UserProfileDto(
                principal.getSubject(),
                principal.getEmail(),
                principal.getClaimAsString("nickname"),
                principal.getClaimAsString("roles"),
                principal.getClaimAsString("warehouseId")
        );

        ResponseEntity<UserProfileDto> response = ResponseEntity
                .status(HttpStatus.OK)
                .body(profile);

        return Mono.just(response);
    }
}
