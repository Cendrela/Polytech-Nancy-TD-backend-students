package com.example.todoapp.présentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateTaskDTO(
        @JsonProperty("title")       String title,
        @JsonProperty("description") String description
) {}
