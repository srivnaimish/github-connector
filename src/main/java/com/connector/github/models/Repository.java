package com.connector.github.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    private String name;
    @JsonProperty("full_name")
    private String fullName;
    private String description;
    private String url;
    private String html_url;
    private String git_url;
    @JsonProperty("private")
    private boolean isPrivate;
    private User owner;

}

