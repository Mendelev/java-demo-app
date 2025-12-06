package com.example.todoapp;

import com.example.todoapp.api.dto.TodoRequest;
import com.example.todoapp.api.dto.TodoResponse;
import com.example.todoapp.model.TodoStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndListTodo() throws Exception {
        TodoResponse created = createTodo("Test task", TodoStatus.PENDING, "Do something important");

        MvcResult result = mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andReturn();

        List<TodoResponse> todos = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertThat(todos).extracting(TodoResponse::getId).contains(created.getId());
        assertThat(todos.getFirst().getTitle()).isEqualTo("Test task");
    }

    @Test
    void updateTodoFields() throws Exception {
        TodoResponse created = createTodo("Old title", TodoStatus.PENDING, "desc");

        TodoRequest update = new TodoRequest();
        update.setTitle("New title");
        update.setDescription("New description");
        update.setStatus(TodoStatus.IN_PROGRESS);
        update.setDueDate(LocalDate.now().plusDays(2));

        MvcResult result = mockMvc.perform(put("/api/todos/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andReturn();

        TodoResponse updated =
                objectMapper.readValue(result.getResponse().getContentAsString(), TodoResponse.class);
        assertThat(updated.getTitle()).isEqualTo("New title");
        assertThat(updated.getDescription()).isEqualTo("New description");
        assertThat(updated.getStatus()).isEqualTo(TodoStatus.IN_PROGRESS);
        assertThat(updated.getDueDate()).isEqualTo(update.getDueDate());
    }

    @Test
    void updateStatusOnly() throws Exception {
        TodoResponse created = createTodo("Do laundry", TodoStatus.PENDING, null);

        MvcResult result = mockMvc.perform(post("/api/todos/" + created.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andReturn();

        TodoResponse updated =
                objectMapper.readValue(result.getResponse().getContentAsString(), TodoResponse.class);
        assertThat(updated.getStatus()).isEqualTo(TodoStatus.DONE);
    }

    @Test
    void deleteTodoRemovesIt() throws Exception {
        TodoResponse created = createTodo("Disposable", TodoStatus.PENDING, null);

        mockMvc.perform(delete("/api/todos/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/todos/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationFailsWithoutTitle() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setDescription("Missing title");

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filterByStatusReturnsMatchingItems() throws Exception {
        createTodo("Pending task", TodoStatus.PENDING, null);
        createTodo("Completed", TodoStatus.DONE, null);

        MvcResult result = mockMvc.perform(get("/api/todos").param("status", "DONE"))
                .andExpect(status().isOk())
                .andReturn();

        List<TodoResponse> todos = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertThat(todos).isNotEmpty();
        assertThat(todos).allMatch(t -> t.getStatus() == TodoStatus.DONE);
    }

    private TodoResponse createTodo(String title, TodoStatus status, String description) throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setStatus(status);

        MvcResult result = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), TodoResponse.class);
    }
}
