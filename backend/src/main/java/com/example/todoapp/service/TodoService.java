package com.example.todoapp.service;

import com.example.todoapp.api.dto.TodoRequest;
import com.example.todoapp.api.dto.TodoResponse;
import com.example.todoapp.exception.NotFoundException;
import com.example.todoapp.model.Todo;
import com.example.todoapp.model.TodoStatus;
import com.example.todoapp.repository.TodoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<TodoResponse> list(TodoStatus status, LocalDate fromDate, LocalDate toDate) {
        return repository.search(status, fromDate, toDate).stream()
                .map(TodoResponse::from)
                .toList();
    }

    public TodoResponse get(UUID id) {
        return TodoResponse.from(findOrThrow(id));
    }

    public TodoResponse create(TodoRequest request) {
        Todo todo = new Todo();
        copy(request, todo);
        return TodoResponse.from(repository.save(todo));
    }

    public TodoResponse update(UUID id, TodoRequest request) {
        Todo todo = findOrThrow(id);
        copy(request, todo);
        return TodoResponse.from(todo);
    }

    public TodoResponse updateStatus(UUID id, TodoStatus status) {
        Todo todo = findOrThrow(id);
        todo.setStatus(status);
        return TodoResponse.from(todo);
    }

    public void delete(UUID id) {
        Todo todo = findOrThrow(id);
        repository.delete(todo);
    }

    private Todo findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Todo not found"));
    }

    private void copy(TodoRequest request, Todo todo) {
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            todo.setStatus(request.getStatus());
        }
        todo.setDueDate(request.getDueDate());
    }
}
