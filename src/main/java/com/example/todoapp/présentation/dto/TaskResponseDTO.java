package com.example.todoapp.présentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.todoapp.business.model.Task;

public record TaskResponseDTO(
        @JsonProperty("id")          Integer id,
        @JsonProperty("title")       String title,
        @JsonProperty("description") String description,
        @JsonProperty("done")        boolean done
) {
    public static TaskResponseDTO from(Task task) {
        return new TaskResponseDTO(task.id(), task.title(), task.description(), task.done());
    }
}
