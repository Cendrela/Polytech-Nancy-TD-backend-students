package com.example.todoapp.business.service;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.persistence.TaskDao;
import com.example.todoapp.présentation.dto.CreateTaskDTO;
import com.example.todoapp.présentation.dto.UpdateTaskDTO;

import java.util.List;
import java.util.Optional;


public class TaskService {

    private final TaskDao dao;

    public TaskService(TaskDao dao) {
        this.dao = dao;
    }

    public Task create(CreateTaskDTO dto) {
        // id=null → SQLite AUTOINCREMENT; done is always false on creation
        Task task = new Task(null, dto.title(), dto.description(), false);
        return dao.save(task);
    }

    public Optional<Task> findById(int id) {
        return dao.findById(id);
    }

    public List<Task> findAll(boolean todoOnly) {
        return todoOnly ? dao.findAllTodo() : dao.findAll();
    }

    public boolean update(int id, UpdateTaskDTO dto) {
        Task task = new Task(id, dto.title(), dto.description(), dto.done());
        return dao.update(id, task);
    }

    public boolean deleteById(int id) {
        return dao.deleteById(id);
    }

    public void deleteAll() {
        dao.deleteAll();
    }

    public int count() {
        return dao.count();
    }
}