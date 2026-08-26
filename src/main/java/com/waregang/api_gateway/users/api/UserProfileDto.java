package com.waregang.api_gateway.users.api;

import org.jspecify.annotations.Nullable;

public record UserProfileDto (
        @Nullable String subject,
        @Nullable String email,
        @Nullable String nickname,
        @Nullable String roles,
        @Nullable String warehouseId
){}
