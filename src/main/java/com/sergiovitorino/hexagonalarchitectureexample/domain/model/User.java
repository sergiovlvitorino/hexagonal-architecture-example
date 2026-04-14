package com.sergiovitorino.hexagonalarchitectureexample.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private UUID id;
    private String name;

    public User(String name) {
        this.name = name;
    }
}
