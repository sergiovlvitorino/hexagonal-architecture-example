package com.sergiovitorino.hexagonalarchitectureexample.application.user;

import com.sergiovitorino.hexagonalarchitectureexample.domain.model.User;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
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
