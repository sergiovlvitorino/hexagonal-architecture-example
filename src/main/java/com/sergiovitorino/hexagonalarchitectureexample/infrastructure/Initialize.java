package com.sergiovitorino.hexagonalarchitectureexample.infrastructure;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class Initialize {

    @Autowired private UserRepository repository;

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
