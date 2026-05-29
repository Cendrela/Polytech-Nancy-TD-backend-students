package com.example.todoapp.présentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorDTO(
        @JsonProperty("field")   String field,
        @JsonProperty("message") String message
) {}
