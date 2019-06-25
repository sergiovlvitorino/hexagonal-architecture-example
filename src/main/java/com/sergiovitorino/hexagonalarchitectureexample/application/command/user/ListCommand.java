package com.sergiovitorino.hexagonalarchitectureexample.application.command.user;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListCommand {

    @Min(0)
    private Integer pageNumber;
    @Min(1)
    private Integer pageSize;
    @NotBlank
    private String orderBy;
    @NotNull
    private Boolean asc;

    private User user;

}
