package com.waregang.api_gateway.users.api;

public record UserProfileDto(
        String subject,
        String email,
        String nickname,
        String roles,
        String warehouseId
) {}
