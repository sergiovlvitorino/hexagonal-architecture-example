package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.controller;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.DeleteCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.UpdateCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

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

    @QueryMapping
    public UserResponse findById(@Argument UUID id) {
        return UserResponse.from(commandHandler.findById(id));
    }

    @MutationMapping
    public UserResponse updateUser(@Argument UUID id, @Argument String name) {
        return UserResponse.from(commandHandler.handle(new UpdateCommand(id, name)));
    }

    @MutationMapping
    public Boolean deleteUser(@Argument UUID id) {
        commandHandler.handle(new DeleteCommand(id));
        return true;
    }
}
