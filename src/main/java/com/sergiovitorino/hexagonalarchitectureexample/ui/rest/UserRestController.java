package com.sergiovitorino.hexagonalarchitectureexample.ui.rest;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rest/user")
@Validated
public class UserRestController {

    private final UserCommandHandler commandHandler;

    public UserRestController(UserCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> get(@Valid ListCommand command) {
        var page = commandHandler.handle(command).map(UserResponse::from);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse post(@RequestBody @Valid SaveCommand command) {
        return UserResponse.from(commandHandler.handle(command));
    }

}
