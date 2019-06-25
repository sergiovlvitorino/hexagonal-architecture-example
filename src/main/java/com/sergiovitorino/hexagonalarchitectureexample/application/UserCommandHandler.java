package com.sergiovitorino.hexagonalarchitectureexample.application;

import com.sergiovitorino.hexagonalarchitectureexample.application.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserCommandHandler {

    @Autowired private UserService service;

    public Page<User> handle(ListCommand command) {
        return service.findAll(command.getPageNumber(), command.getPageSize(), command.getOrderBy(), command.getAsc(), command.getUser() == null ? new User() : command.getUser());
    }
}
