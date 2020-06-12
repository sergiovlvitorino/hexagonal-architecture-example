package com.sergiovitorino.hexagonalarchitectureexample.application.command.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.validations.SafeHtml;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveCommand {

    @NotEmpty
    @Size(min = 5, max = 100)
    @SafeHtml
    private String name;

}
