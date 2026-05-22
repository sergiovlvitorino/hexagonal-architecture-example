package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.test;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.DeleteCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.UpdateCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.DomainValidationException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.exception.UserNotFoundException;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.controller.UserGraphQLController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(UserGraphQLController.class)
public class UserGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private UserCommandHandler commandHandler;

    private User createUser(String name) {
        var user = new User(name);
        user.setId(UUID.randomUUID());
        return user;
    }

    @Test
    public void testIfListCommandIsOk() {
        var users = List.of(createUser("Alice"), createUser("Bob"));
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(users));

        graphQlTester.document("""
                    { findAll(pageNumber: 0, pageSize: 100, orderBy: "name", asc: true) {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .path("findAll.content").entityList(Object.class).hasSize(2);

        graphQlTester.document("""
                    { findAll(pageNumber: 0, pageSize: 100, orderBy: "name", asc: true) {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .path("findAll.totalElements").entity(Integer.class).isEqualTo(2);
    }

    @Test
    public void testIfListCommandWithUserNameFilterIsOk() {
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(List.of(), Pageable.ofSize(100), 0));

        graphQlTester.document("""
                    { findAll(pageNumber: 0, pageSize: 100, orderBy: "name", asc: true, userName: "nonexistent") {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .path("findAll.totalElements").entity(Integer.class).isEqualTo(0);
    }

    @Test
    public void testIfNegativePageNumberReturnsError() {
        when(commandHandler.handle(any(ListCommand.class)))
                .thenThrow(new DomainValidationException("pageNumber must be >= 0"));

        graphQlTester.document("""
                    { findAll(pageNumber: -1, pageSize: 10, orderBy: "name", asc: true) {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    public void testIfPageSizeExceedingLimitReturnsError() {
        when(commandHandler.handle(any(ListCommand.class)))
                .thenThrow(new DomainValidationException("pageSize must be between 1 and 1000"));

        graphQlTester.document("""
                    { findAll(pageNumber: 0, pageSize: 5000, orderBy: "name", asc: true) {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    public void testIfPageSizeZeroReturnsError() {
        when(commandHandler.handle(any(ListCommand.class)))
                .thenThrow(new DomainValidationException("pageSize must be between 1 and 1000"));

        graphQlTester.document("""
                    { findAll(pageNumber: 0, pageSize: 0, orderBy: "name", asc: true) {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    public void testIfInvalidOrderByReturnsError() {
        when(commandHandler.handle(any(ListCommand.class)))
                .thenThrow(new DomainValidationException("orderBy must be one of: [id, name]"));

        graphQlTester.document("""
                    { findAll(pageNumber: 0, pageSize: 10, orderBy: "email", asc: true) {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    public void findById_existing_returnsUserResponse() {
        var user = createUser("Alice");
        when(commandHandler.findById(user.getId())).thenReturn(user);

        graphQlTester.document("""
                    { findById(id: "%s") { id name } }
                """.formatted(user.getId()))
                .execute()
                .path("findById.name").entity(String.class).isEqualTo("Alice");
    }

    @Test
    public void findById_nonExisting_returnsErrorsPopulatedAndDataNull() {
        var id = UUID.randomUUID();
        when(commandHandler.findById(id)).thenThrow(new UserNotFoundException(id));

        var result = graphQlTester.document("""
                    { findById(id: "%s") { id name } }
                """.formatted(id))
                .execute();

        result.errors().satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    public void updateUser_existing_returnsUpdatedUser() {
        var id = UUID.randomUUID();
        var updated = new User(id, "UpdatedName");
        when(commandHandler.handle(any(UpdateCommand.class))).thenReturn(updated);

        graphQlTester.document("""
                    mutation { updateUser(id: "%s", name: "UpdatedName") { id name } }
                """.formatted(id))
                .execute()
                .path("updateUser.name").entity(String.class).isEqualTo("UpdatedName");
    }

    @Test
    public void updateUser_nonExisting_returnsError() {
        var id = UUID.randomUUID();
        when(commandHandler.handle(any(UpdateCommand.class))).thenThrow(new UserNotFoundException(id));

        graphQlTester.document("""
                    mutation { updateUser(id: "%s", name: "NewName") { id name } }
                """.formatted(id))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    public void deleteUser_existing_returnsTrue() {
        var id = UUID.randomUUID();
        // handle(DeleteCommand) é void — sem mock necessário

        graphQlTester.document("""
                    mutation { deleteUser(id: "%s") }
                """.formatted(id))
                .execute()
                .path("deleteUser").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    public void deleteUser_nonExisting_returnsError() {
        var id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new UserNotFoundException(id))
                .when(commandHandler).handle(any(DeleteCommand.class));

        graphQlTester.document("""
                    mutation { deleteUser(id: "%s") }
                """.formatted(id))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    public void testIfOrderByIdIsOk() {
        var users = List.of(createUser("Alice"), createUser("Bob"));
        when(commandHandler.handle(any(ListCommand.class)))
                .thenReturn(new PageImpl<>(users));

        graphQlTester.document("""
                    { findAll(pageNumber: 0, pageSize: 10, orderBy: "id", asc: true) {
                        content { id name }
                        totalElements
                    }}
                """)
                .execute()
                .path("findAll.totalElements").entity(Integer.class).satisfies(total -> assertThat(total).isGreaterThan(0));
    }
}
