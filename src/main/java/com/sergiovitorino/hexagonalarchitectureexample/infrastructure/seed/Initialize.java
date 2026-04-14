package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.seed;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserEntity;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!prod")
public class Initialize {

    private final UserRepository repository;

    public Initialize(UserRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void execute() {
        repository.saveAll(List.of(
                new UserEntity(UUID.randomUUID().toString()),
                new UserEntity(UUID.randomUUID().toString()),
                new UserEntity(UUID.randomUUID().toString()),
                new UserEntity(UUID.randomUUID().toString()),
                new UserEntity(UUID.randomUUID().toString()),
                new UserEntity(UUID.randomUUID().toString())
        ));
    }
}
