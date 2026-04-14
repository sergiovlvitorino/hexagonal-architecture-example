package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.controller;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQLController {

    private final UserCommandHandler commandHandler;

    public UserGraphQLController(final UserCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @QueryMapping
    public Page<UserResponse> findAll(@Argument final Integer pageNumber, @Argument final Integer pageSize,
                              @Argument final String orderBy, @Argument final Boolean asc,
                              @Argument final String userName) {
        return commandHandler.handle(new ListCommand(pageNumber, pageSize, orderBy, asc, userName))
                .map(UserResponse::from);
    }

}
