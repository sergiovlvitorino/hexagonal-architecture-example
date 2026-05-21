package com.sergiovitorino.hexagonalarchitectureexample.application.command;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.DeleteCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.UpdateCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserCommandHandler {

    private static final Set<String> ALLOWED_ORDER_FIELDS = Set.of("id", "name");

    private final UserService service;
    private final int maxPageSize;

    public UserCommandHandler(UserService service,
                              @Value("${pagination.max-page-size:1000}") int maxPageSize) {
        this.service = service;
        this.maxPageSize = maxPageSize;
    }

    public Page<User> handle(final ListCommand command) {
        if (command.pageNumber() == null || command.pageNumber() < 0)
            throw new IllegalArgumentException("pageNumber must be >= 0");
        if (command.pageSize() == null || command.pageSize() < 1 || command.pageSize() > maxPageSize)
            throw new IllegalArgumentException("pageSize must be between 1 and " + maxPageSize);
        if (command.orderBy() == null || command.orderBy().isBlank())
            throw new IllegalArgumentException("orderBy must not be blank");
        if (command.asc() == null)
            throw new IllegalArgumentException("asc must not be null");
        if (!ALLOWED_ORDER_FIELDS.contains(command.orderBy())) {
            throw new IllegalArgumentException("orderBy must be one of: " + ALLOWED_ORDER_FIELDS);
        }
        return service.findAll(command.pageNumber(), command.pageSize(), command.orderBy(), command.asc(), command.userName());
    }

    public User handle(final SaveCommand command) {
        return service.save(new User(command.name()));
    }

    public User findById(UUID id) {
        return service.findById(id);
    }

    public User handle(final UpdateCommand command) {
        return service.update(command.id(), command.name());
    }

    public void handle(final DeleteCommand command) {
        service.delete(command.id());
    }
}
