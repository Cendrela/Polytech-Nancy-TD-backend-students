package com.example.todoapp.présentation;

import com.example.todoapp.business.service.TaskService;
import com.example.todoapp.persistence.TaskDao;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        TaskDao dao = new TaskDao();
        TaskService service = new TaskService(dao);
        TaskController controller = new TaskController(service);

        log.info("In-memory repository initialised");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", controller::handle);
        server.setExecutor(null);
        server.start();

        log.info("HTTP server started on http://localhost:8080");
    }
}