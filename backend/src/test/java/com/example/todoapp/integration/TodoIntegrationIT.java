package com.example.todoapp.integration;

import com.example.todoapp.TodoApplication;
import com.example.todoapp.api.dto.TodoRequest;
import com.example.todoapp.api.dto.TodoResponse;
import com.example.todoapp.model.TodoStatus;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(
        classes = TodoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class TodoIntegrationIT {

    private static final String HOST = System.getenv().getOrDefault("TEST_DB_HOST", "localhost");
    private static final String PORT = System.getenv().getOrDefault("TEST_DB_PORT", "55432");
    private static final String DB = System.getenv().getOrDefault("TEST_DB_NAME", "todoapp_int");
    private static final String USER = System.getenv().getOrDefault("TEST_DB_USER", "todo");
    private static final String PASSWORD = System.getenv().getOrDefault("TEST_DB_PASSWORD", "todo");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB);
        registry.add("spring.datasource.username", () -> USER);
        registry.add("spring.datasource.password", () -> PASSWORD);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullCrudFlowAgainstExternalPostgres() {
        TodoRequest create = new TodoRequest();
        create.setTitle("Integration task");
        create.setDescription("Testing end to end");
        create.setStatus(TodoStatus.PENDING);
        create.setDueDate(LocalDate.now().plusDays(1));

        ResponseEntity<TodoResponse> createdResp = restTemplate.postForEntity(url("/api/todos"), create, TodoResponse.class);
        Assertions.assertThat(createdResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TodoResponse created = createdResp.getBody();
        Assertions.assertThat(created).isNotNull();
        UUID id = created.getId();

        TodoResponse fetched = restTemplate.getForObject(url("/api/todos/" + id), TodoResponse.class);
        Assertions.assertThat(fetched.getTitle()).isEqualTo("Integration task");

        TodoRequest update = new TodoRequest();
        update.setTitle("Updated title");
        update.setDescription("Updated desc");
        update.setStatus(TodoStatus.IN_PROGRESS);
        update.setDueDate(LocalDate.now().plusDays(3));

        ResponseEntity<TodoResponse> updatedResp = restTemplate.exchange(
                url("/api/todos/" + id), HttpMethod.PUT, new HttpEntity<>(update, new HttpHeaders()), TodoResponse.class);
        Assertions.assertThat(updatedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        TodoResponse updated = updatedResp.getBody();
        Assertions.assertThat(updated).isNotNull();
        Assertions.assertThat(updated.getStatus()).isEqualTo(TodoStatus.IN_PROGRESS);
        Assertions.assertThat(updated.getTitle()).isEqualTo("Updated title");

        ResponseEntity<TodoResponse> statusResp = restTemplate.postForEntity(
                url("/api/todos/" + id + "/status"), new StatusPayload(TodoStatus.DONE), TodoResponse.class);
        Assertions.assertThat(statusResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(statusResp.getBody()).isNotNull();
        Assertions.assertThat(statusResp.getBody().getStatus()).isEqualTo(TodoStatus.DONE);

        ResponseEntity<TodoResponse[]> listResp =
                restTemplate.getForEntity(url("/api/todos?status=DONE"), TodoResponse[].class);
        Assertions.assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<TodoResponse> todos = Arrays.asList(listResp.getBody());
        Assertions.assertThat(todos).extracting(TodoResponse::getId).contains(id);

        restTemplate.delete(url("/api/todos/" + id));
        ResponseEntity<String> afterDelete =
                restTemplate.getForEntity(url("/api/todos/" + id), String.class);
        Assertions.assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private record StatusPayload(TodoStatus status) {}
}
