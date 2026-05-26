package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.seed;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserEntity;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@Profile("!prod")
public class Initialize {

    private final UserRepository repository;

    public Initialize(UserRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void execute() {
        var users = IntStream.rangeClosed(1, 6)
                .mapToObj(i -> new UserEntity("User-" + i + "-" + UUID.randomUUID().toString().substring(0, 6)))
                .toList();
        repository.saveAll(users);
    }
}
