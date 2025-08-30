package com.connector.github.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControllerResponse<D>{
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private D response;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;
}
