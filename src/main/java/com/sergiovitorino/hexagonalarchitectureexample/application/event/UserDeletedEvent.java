package com.sergiovitorino.hexagonalarchitectureexample.application.event;

import java.util.UUID;

public record UserDeletedEvent(UUID id) {}
