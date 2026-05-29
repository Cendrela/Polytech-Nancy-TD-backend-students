package com.example.todoapp.présentation.dto;

import java.util.ArrayList;
import java.util.List;


public class DtoValidator {

    private static final int MAX_TITLE_LENGTH       = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private DtoValidator() {}


    public static List<ErrorDTO> validate(CreateTaskDTO dto) {
        List<ErrorDTO> errors = new ArrayList<>();

        if (dto == null) {
            errors.add(new ErrorDTO("body", "Request body is missing or malformed"));
            return errors;
        }

        // title
        if (dto.title() == null || dto.title().isBlank()) {
            errors.add(new ErrorDTO("title", "title is required"));
        } else if (dto.title().length() > MAX_TITLE_LENGTH) {
            errors.add(new ErrorDTO("title",
                    "title must not exceed " + MAX_TITLE_LENGTH + " characters (got " + dto.title().length() + ")"));
        }

        // description
        if (dto.description() != null && dto.description().length() > MAX_DESCRIPTION_LENGTH) {
            errors.add(new ErrorDTO("description",
                    "description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters (got " + dto.description().length() + ")"));
        }

        return errors;
    }

    public static List<ErrorDTO> validate(UpdateTaskDTO dto) {
        List<ErrorDTO> errors = new ArrayList<>();

        if (dto == null) {
            errors.add(new ErrorDTO("body", "Request body is missing or malformed"));
            return errors;
        }

        // title
        if (dto.title() == null || dto.title().isBlank()) {
            errors.add(new ErrorDTO("title", "title is required"));
        } else if (dto.title().length() > MAX_TITLE_LENGTH) {
            errors.add(new ErrorDTO("title",
                    "title must not exceed " + MAX_TITLE_LENGTH + " characters (got " + dto.title().length() + ")"));
        }

        // description
        if (dto.description() != null && dto.description().length() > MAX_DESCRIPTION_LENGTH) {
            errors.add(new ErrorDTO("description",
                    "description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters (got " + dto.description().length() + ")"));
        }

        // done
        if (dto.done() == null) {
            errors.add(new ErrorDTO("done", "done is required"));
        }

        return errors;
    }
}
