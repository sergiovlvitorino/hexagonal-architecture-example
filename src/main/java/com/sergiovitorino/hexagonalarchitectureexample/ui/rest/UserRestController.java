package com.sergiovitorino.hexagonalarchitectureexample.framework.rest;

import com.sergiovitorino.hexagonalarchitectureexample.application.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/rest/user")
@Validated
public class UserRestController {

    @Autowired private UserCommandHandler commandHandler;

    @GetMapping
    public Page<User> get(@Valid ListCommand command){
        return commandHandler.handle(command);
    }

}
