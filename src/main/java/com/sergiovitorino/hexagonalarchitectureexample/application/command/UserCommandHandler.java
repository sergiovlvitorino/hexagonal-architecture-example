package com.sergiovitorino.hexagonalarchitectureexample.application.command;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.service.UserService;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserCommandHandler {

    @Autowired private UserService service;

    public Page<User> handle(final ListCommand command) {
        return service.findAll(command.pageNumber(), command.pageSize(), command.orderBy(), command.asc(),
                command.user() == null ? new User() : command.user());
    }

    public User handle(final SaveCommand command) {
        return service.save(new User(command.name()));
    }
}
