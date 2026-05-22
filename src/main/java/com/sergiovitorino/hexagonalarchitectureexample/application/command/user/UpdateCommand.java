package com.sergiovitorino.hexagonalarchitectureexample.application.command.user;

import com.sergiovitorino.hexagonalarchitectureexample.application.validation.SafeHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCommand(
        @NotNull UUID id,
        @NotBlank @SafeHtml @Size(min = 5, max = 100) String name
) {}
