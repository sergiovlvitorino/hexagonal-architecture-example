package com.sergiovitorino.hexagonalarchitectureexample.application.dto;

import com.sergiovitorino.hexagonalarchitectureexample.application.validation.SafeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// API contract — keep validation rules in sync with UpdateCommand.name
public record UpdateUserRequest(
        @NotBlank @SafeHtml @Size(min = 5, max = 100) String name
) {}
