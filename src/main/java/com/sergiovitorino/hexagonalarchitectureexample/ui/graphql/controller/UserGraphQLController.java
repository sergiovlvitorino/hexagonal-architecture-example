package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.controller;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQLController {

    @Value("${pagination.max-page-size:1000}")
    private int maxPageSize;

    private final UserCommandHandler commandHandler;

    public UserGraphQLController(final UserCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @QueryMapping
    public Page<User> findAll(@Argument final Integer pageNumber, @Argument final Integer pageSize,
                              @Argument final String orderBy, @Argument final Boolean asc,
                              @Argument final String userName) {
        if (pageNumber == null || pageNumber < 0) throw new IllegalArgumentException("pageNumber must be >= 0");
        if (pageSize == null || pageSize < 1 || pageSize > maxPageSize) throw new IllegalArgumentException("pageSize must be between 1 and " + maxPageSize);
        if (orderBy == null) throw new IllegalArgumentException("orderBy must not be null");
        if (asc == null) throw new IllegalArgumentException("asc must not be null");

        var user = userName != null ? new User(userName) : null;
        return commandHandler.handle(new ListCommand(pageNumber, pageSize, orderBy, asc, user));
    }

}
