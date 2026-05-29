package com.example.todoapp.présentation;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.business.service.TaskService;
import com.example.todoapp.présentation.dto.*;
import com.example.todoapp.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;


public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();
        log.debug("{} {}", method, path);

        try {
            if ("POST".equals(method) && "/tasks".equals(path)) {
                handleCreate(exchange);
            } else if ("GET".equals(method) && "/tasks/count".equals(path)) {
                handleCount(exchange);
            } else if ("GET".equals(method) && "/tasks".equals(path)) {
                handleFindAll(exchange);
            } else if ("DELETE".equals(method) && "/tasks".equals(path)) {
                handleDeleteAll(exchange);
            } else {
                Matcher m = ID_PATH.matcher(path);
                if (m.matches()) {
                    int id = Integer.parseInt(m.group(1));
                    switch (method) {
                        case "GET"    -> handleFindById(exchange, id);
                        case "PUT"    -> handleUpdate(exchange, id);
                        case "DELETE" -> handleDeleteById(exchange, id);
                        default       -> sendResponse(exchange, 405, null);
                    }
                } else {
                    sendResponse(exchange, 404, null);
                }
            }
        } catch (Exception e) {
            log.error("Unhandled error while processing {} {}", method, path, e);
            try {
                ErrorDTO error = new ErrorDTO("error", "An unexpected error occurred: " + e.getMessage());
                sendResponse(exchange, 500, JsonUtils.serialize(error));
            } catch (Exception serialisationFailure) {
                // Last-resort fallback: JSON serialisation itself failed
                log.error("Failed to serialise 500 error response", serialisationFailure);
                sendResponse(exchange, 500, null);
            }
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
        CreateTaskDTO dto = JsonUtils.deserialize(body, CreateTaskDTO.class);

        List<ErrorDTO> errors = DtoValidator.validate(dto);
        if (!errors.isEmpty()) {
            sendResponse(exchange, 400, JsonUtils.serialize(errors.get(0)));
            return;
        }

        Task created = taskService.create(dto);
        exchange.getResponseHeaders().add("Location", "/tasks/" + created.id());
        sendResponse(exchange, 201, JsonUtils.serialize(TaskResponseDTO.from(created)));
    }

    private void handleFindById(HttpExchange exchange, int id) throws IOException {
        Optional<Task> task = taskService.findById(id);
        if (task.isPresent()) {
            sendResponse(exchange, 200, JsonUtils.serialize(TaskResponseDTO.from(task.get())));
        } else {
            sendResponse(exchange, 404, null);
        }
    }

    private void handleFindAll(HttpExchange exchange) throws IOException {
        String query    = exchange.getRequestURI().getQuery();
        boolean todoOnly = query != null && query.contains("todo-only=true");
        List<Task> tasks = taskService.findAll(todoOnly);
        if (tasks.isEmpty()) {
            sendResponse(exchange, 204, null);
        } else {
            List<TaskResponseDTO> dtos = tasks.stream()
                    .map(TaskResponseDTO::from)
                    .toList();
            sendResponse(exchange, 200, JsonUtils.serialize(dtos));
        }
    }

    private void handleUpdate(HttpExchange exchange, int id) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
        UpdateTaskDTO dto = JsonUtils.deserialize(body, UpdateTaskDTO.class);

        List<ErrorDTO> errors = DtoValidator.validate(dto);
        if (!errors.isEmpty()) {
            sendResponse(exchange, 400, JsonUtils.serialize(errors.get(0)));
            return;
        }

        boolean updated = taskService.update(id, dto);
        sendResponse(exchange, updated ? 204 : 404, null);
    }

    private void handleDeleteById(HttpExchange exchange, int id) throws IOException {
        boolean deleted = taskService.deleteById(id);
        sendResponse(exchange, deleted ? 204 : 404, null);
    }

    private void handleDeleteAll(HttpExchange exchange) throws IOException {
        taskService.deleteAll();
        sendResponse(exchange, 204, null);
    }

    private void handleCount(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, String.valueOf(taskService.count()));
    }


    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}