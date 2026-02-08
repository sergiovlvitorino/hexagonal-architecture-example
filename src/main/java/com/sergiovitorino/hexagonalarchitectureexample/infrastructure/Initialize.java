package com.sergiovitorino.hexagonalarchitectureexample.infrastructure;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

@Component
@Profile("!prod")
public class Initialize {

    private final UserRepository repository;

    public Initialize(UserRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void execute(){
        repository.save(new User(null, UUID.randomUUID().toString()));
        repository.save(new User(null, UUID.randomUUID().toString()));
        repository.save(new User(null, UUID.randomUUID().toString()));
        repository.save(new User(null, UUID.randomUUID().toString()));
        repository.save(new User(null, UUID.randomUUID().toString()));
        repository.save(new User(null, UUID.randomUUID().toString()));
    }

}
