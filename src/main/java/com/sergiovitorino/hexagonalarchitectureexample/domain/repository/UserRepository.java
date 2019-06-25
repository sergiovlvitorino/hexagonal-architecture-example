package com.sergiovitorino.hexagonalarchitectureexample.domain.repository;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
