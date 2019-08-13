package com.sergiovitorino.hexagonalarchitectureexample.application.service;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired private UserRepository repository;

    public Page<User> findAll(Integer pageNumber, Integer pageSize, String orderBy, Boolean asc, User user) {
        final Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final Sort sort = Sort.by(direction, orderBy);
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        final ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final Example<User> example = Example.of(user, matcher);
        return repository.findAll(example, pageable);
    }

    public User save(User user) {
        return repository.save(user);
    }
}
