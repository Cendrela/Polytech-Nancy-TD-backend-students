package com.example.todoapp.présentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateTaskDTO(
        @JsonProperty("title")       String title,
        @JsonProperty("description") String description,
        @JsonProperty("done")        Boolean done
) {}
