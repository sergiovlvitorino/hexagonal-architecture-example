package com.sergiovitorino.hexagonalarchitectureexample.application.dto;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;

import java.util.UUID;

public record UserResponse(UUID id, String name) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName());
    }
}
