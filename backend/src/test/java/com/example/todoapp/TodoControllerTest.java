package com.example.todoapp;

import com.example.todoapp.api.dto.TodoRequest;
import com.example.todoapp.api.dto.TodoResponse;
import com.example.todoapp.model.TodoStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndListTodo() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("Test task");
        request.setDescription("Do something important");
        request.setStatus(TodoStatus.PENDING);

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andReturn();

        List<TodoResponse> todos = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertThat(todos).isNotEmpty();
        assertThat(todos.get(0).getTitle()).isEqualTo("Test task");
    }
}
