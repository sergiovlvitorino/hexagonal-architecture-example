package com.sergiovitorino.hexagonalarchitectureexample.ui.rest;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public User post(@RequestBody @Valid SaveCommand command){ return commandHandler.handle(command); }

}