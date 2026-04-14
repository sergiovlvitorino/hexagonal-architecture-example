package com.sergiovitorino.hexagonalarchitectureexample.domain.repository;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryPort {
    Page<User> findAll(String userName, Pageable pageable);
    User save(User user);
}
