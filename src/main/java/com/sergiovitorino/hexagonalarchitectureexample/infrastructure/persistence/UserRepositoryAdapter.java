package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.persistence;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.repository.UserRepositoryPort;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    public UserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<User> findAll(String userName, Pageable pageable) {
        var entity = new UserEntity();
        if (userName != null) {
            entity.setName(userName);
        }
        var matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase();
        var example = Example.of(entity, matcher);
        return userRepository.findAll(example, pageable).map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        var entity = UserEntity.fromDomain(user);
        var saved = userRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }
}
