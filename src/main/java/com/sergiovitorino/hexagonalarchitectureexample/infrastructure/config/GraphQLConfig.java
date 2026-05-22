package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config;

import graphql.analysis.MaxQueryDepthInstrumentation;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GraphQLConfig {

    @Bean
    public GraphQlSourceBuilderCustomizer maxDepthCustomizer() {
        return builder -> builder.instrumentation(List.of(new MaxQueryDepthInstrumentation(10)));
    }
}
