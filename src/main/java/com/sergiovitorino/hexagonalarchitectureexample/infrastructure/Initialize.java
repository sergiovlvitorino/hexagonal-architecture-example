package com.sergiovitorino.hexagonalarchitectureexample.infrastructure;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@Profile("!prod")
public class Initialize {

    private static final int SEED_USER_COUNT = 6;

    private final UserRepository repository;

    public Initialize(UserRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void execute() {
        IntStream.range(0, SEED_USER_COUNT)
                .mapToObj(i -> new User(null, UUID.randomUUID().toString()))
                .forEach(repository::save);
    }

}
