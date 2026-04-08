package com.sergiovitorino.hexagonalarchitectureexample.application.command.user;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ListCommand(
        @Min(0) Integer pageNumber,
        @Min(1) @Max(1000) Integer pageSize,
        @NotBlank String orderBy,
        @NotNull Boolean asc,
        User user
) {
}
