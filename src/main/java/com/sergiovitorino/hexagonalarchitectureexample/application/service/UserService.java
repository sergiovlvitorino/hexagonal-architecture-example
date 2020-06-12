package com.sergiovitorino.hexagonalarchitectureexample.application.service;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    @Autowired private UserRepository repository;

    public Page<User> findAll(final Integer pageNumber, final Integer pageSize, final String orderBy, final boolean asc,
            final User user) {
        final var direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sort = Sort.by(direction, orderBy);
        final var pageable = PageRequest.of(pageNumber, pageSize, sort);
        final var matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase();
        final var example = Example.of(user, matcher);
        return repository.findAll(example, pageable);
    }

    public User save(final User user) {
        return repository.save(user);
    }
}
