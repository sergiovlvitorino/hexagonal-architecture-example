package com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.hexagonalarchitectureexample.ui.graphql.mapper.UserMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/graphql/user")
public class UserGraphQLController {

    public static final String QUERY = "query";
    public static final String OPERATION_NAME = "operationName";
    private final GraphQL graphQL;
    @Autowired private ObjectMapper mapper;

    public UserGraphQLController(final UserMapper mapper) {
        final GraphQLSchema schema = new GraphQLSchemaGenerator()
                .withResolverBuilders(
                        //Resolve by annotations
                        new AnnotatedResolverBuilder())
                .withOperationsFromSingleton(mapper)
                .withValueMapperFactory(new JacksonValueMapperFactory())
                .generate();
        graphQL = GraphQL.newGraphQL(schema).build();
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Map<String, Object> graphQL(@RequestBody Map<String, String> request, HttpServletRequest raw) {
        final ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(request.get(QUERY))
                .operationName(request.get(OPERATION_NAME))
                .context(raw)
                .build());
        return executionResult.toSpecification();
    }

}
