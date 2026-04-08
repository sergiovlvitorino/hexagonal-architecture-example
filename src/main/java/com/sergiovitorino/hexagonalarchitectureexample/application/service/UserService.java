package com.sergiovitorino.hexagonalarchitectureexample.application.service;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(final Integer pageNumber, final Integer pageSize, final String orderBy, final boolean asc,
            final User user) {
        log.debug("findAll: page={}, size={}, orderBy={}, asc={}", pageNumber, pageSize, orderBy, asc);
        final var direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sort = Sort.by(direction, orderBy);
        final var pageable = PageRequest.of(pageNumber, pageSize, sort);
        final var matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase();
        final var example = Example.of(user, matcher);
        return repository.findAll(example, pageable);
    }

    @Transactional
    public User save(final User user) {
        log.debug("save: user={}", user.getName());
        return repository.save(user);
    }
}
