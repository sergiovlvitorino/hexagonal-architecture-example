package com.sergiovitorino.hexagonalarchitectureexample.ui.rest;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/user")
@Validated
public class UserRestController {

    private final UserCommandHandler commandHandler;

    public UserRestController(UserCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @GetMapping
    public Page<User> get(@Valid ListCommand command){
        return commandHandler.handle(command);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User post(@RequestBody @Valid SaveCommand command){
        return commandHandler.handle(command);
    }

}
