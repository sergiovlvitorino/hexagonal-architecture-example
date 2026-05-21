package com.sergiovitorino.hexagonalarchitectureexample.domain.repository;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Page<User> findAll(String userName, Pageable pageable);
    User save(User user);
    Optional<User> findById(UUID id);
    void deleteById(UUID id);
}
