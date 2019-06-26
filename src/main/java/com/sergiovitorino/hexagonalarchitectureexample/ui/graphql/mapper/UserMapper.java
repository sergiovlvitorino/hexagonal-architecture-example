package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.mapper;

import com.sergiovitorino.hexagonalarchitectureexample.application.command.UserCommandHandler;
import com.sergiovitorino.hexagonalarchitectureexample.application.command.user.ListCommand;
import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired private UserCommandHandler commandHandler;

    @GraphQLQuery(name = "findAll")
    public Page<User> findAll(@GraphQLNonNull Integer pageNumber, @GraphQLNonNull Integer pageSize, @GraphQLNonNull String orderBy, @GraphQLNonNull Boolean asc, User user){
        return commandHandler.handle(new ListCommand(pageNumber, pageSize, orderBy, asc, user));
    }

}