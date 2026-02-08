package com.sergiovitorino.hexagonalarchitectureexample.application.command.user;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.validations.SafeHtml;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SaveCommand(
        @NotEmpty @Size(min = 5, max = 100) @SafeHtml String name
) {
}
