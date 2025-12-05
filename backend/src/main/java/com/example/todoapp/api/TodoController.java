package com.example.todoapp.api;

import com.example.todoapp.api.dto.TodoRequest;
import com.example.todoapp.api.dto.TodoResponse;
import com.example.todoapp.api.dto.UpdateStatusRequest;
import com.example.todoapp.model.TodoStatus;
import com.example.todoapp.service.TodoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TodoResponse> list(
            @RequestParam(required = false) TodoStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return service.list(status, fromDate, toDate);
    }

    @GetMapping("/{id}")
    public TodoResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@RequestBody @Valid TodoRequest request) {
        TodoResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TodoResponse update(@PathVariable UUID id, @RequestBody @Valid TodoRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/status")
    public TodoResponse updateStatus(
            @PathVariable UUID id, @RequestBody @Valid UpdateStatusRequest request) {
        return service.updateStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
