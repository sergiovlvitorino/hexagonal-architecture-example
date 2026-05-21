package com.sergiovitorino.hexagonalarchitectureexample.application.command.user;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteCommand(@NotNull UUID id) {}
