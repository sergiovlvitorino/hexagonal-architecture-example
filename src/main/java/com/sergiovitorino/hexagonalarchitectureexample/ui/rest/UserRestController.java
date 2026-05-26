package com.sergiovitorino.hexagonalarchitectureexample.ui.rest;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.DeleteCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.SaveCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.UpdateCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.api.UsersApi;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.dto.PagedUserResponse;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.dto.SaveUserRequest;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.dto.UpdateUserRequest;
import com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * REST adapter implementing the OpenAPI-generated {@link UsersApi} interface.
 * The OpenAPI spec at {@code src/main/resources/openapi/users.yaml} is the contract source-of-truth (SDD).
 * Validation (Bean Validation) is applied to generated DTOs; defense-in-depth {@code @SafeHtml} runs in Commands.
 */
@Controller
public class UserRestController implements UsersApi {

    private final UserCommandHandler commandHandler;

    public UserRestController(UserCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public ResponseEntity<PagedUserResponse> listUsers(Integer pageNumber,
                                                       Integer pageSize,
                                                       String orderBy,
                                                       Boolean asc,
                                                       String userName) {
        var command = new ListCommand(pageNumber, pageSize, orderBy, asc, userName);
        var page = commandHandler.handle(command);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(toPagedUserResponse(page));
    }

    @Override
    public ResponseEntity<UserResponse> findUserById(UUID id) {
        return ResponseEntity.ok(toUserResponse(commandHandler.findById(id)));
    }

    @Override
    public ResponseEntity<UserResponse> createUser(SaveUserRequest saveUserRequest) {
        var saved = commandHandler.handle(new SaveCommand(saveUserRequest.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(saved));
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(UUID id, UpdateUserRequest updateUserRequest) {
        var updated = commandHandler.handle(new UpdateCommand(id, updateUserRequest.getName()));
        return ResponseEntity.ok(toUserResponse(updated));
    }

    @Override
    public ResponseEntity<Void> deleteUser(UUID id) {
        commandHandler.handle(new DeleteCommand(id));
        return ResponseEntity.noContent().build();
    }

    // --- mappers domain -> generated DTOs ---

    private static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName());
    }

    private static PagedUserResponse toPagedUserResponse(Page<User> page) {
        var content = page.map(UserRestController::toUserResponse).getContent();
        return new PagedUserResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty(),
                page.getNumberOfElements()
        );
    }
}
