package com.sergiovitorino.hexagonalarchitectureexample.application.command;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class UserCommandHandler {

    private static final Set<String> ALLOWED_ORDER_FIELDS = Set.of("id", "name");

    private final UserService service;

    public UserCommandHandler(UserService service) {
        this.service = service;
    }

    public Page<User> handle(final ListCommand command) {
        if (!ALLOWED_ORDER_FIELDS.contains(command.orderBy())) {
            throw new IllegalArgumentException("orderBy must be one of: " + ALLOWED_ORDER_FIELDS);
        }
        return service.findAll(command.pageNumber(), command.pageSize(), command.orderBy(), command.asc(),
                Optional.ofNullable(command.user()).orElseGet(User::new));
    }

    public User handle(final SaveCommand command) {
        return service.save(new User(command.name()));
    }
}
