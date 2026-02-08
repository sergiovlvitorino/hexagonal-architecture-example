package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.controller;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQLController {

    private static final int MAX_PAGE_SIZE = 1000;
    private static final java.util.Set<String> ALLOWED_ORDER_FIELDS = java.util.Set.of("id", "name");

    private final UserCommandHandler commandHandler;

    public UserGraphQLController(final UserCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @QueryMapping
    public Page<User> findAll(@Argument final Integer pageNumber, @Argument final Integer pageSize,
                              @Argument final String orderBy, @Argument final Boolean asc,
                              @Argument final String userName) {
        if (pageNumber == null || pageNumber < 0) throw new IllegalArgumentException("pageNumber must be >= 0");
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) throw new IllegalArgumentException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        if (orderBy == null || !ALLOWED_ORDER_FIELDS.contains(orderBy)) throw new IllegalArgumentException("orderBy must be one of: " + ALLOWED_ORDER_FIELDS);
        if (asc == null) throw new IllegalArgumentException("asc must not be null");

        var user = userName != null ? new User(userName) : null;
        return commandHandler.handle(new ListCommand(pageNumber, pageSize, orderBy, asc, user));
    }

}
