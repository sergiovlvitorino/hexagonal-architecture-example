package com.sergiovitorino.hexagonalarchitectureexample.application.command.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ListCommand(
        @Min(0) Integer pageNumber,
        @Min(1) @Max(1000) Integer pageSize,
        @NotBlank String orderBy,
        @NotNull Boolean asc,
        @Size(max = 100) String userName
) {
}
