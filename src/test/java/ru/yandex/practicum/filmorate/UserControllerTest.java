package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserStorage userStorage;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    public void clearStorage() {
        ((InMemoryUserStorage) userStorage).clear();
    }

    @BeforeEach
    public void setUp() {
        testUser1 = new User();
        testUser1.setEmail("test1@example.com");
        testUser1.setLogin("user1");
        testUser1.setName("User One");
        testUser1.setBirthday(LocalDate.of(1990, 1, 1));
        testUser1.setFriends(new HashSet<>());

        testUser2 = new User();
        testUser2.setEmail("test2@example.com");
        testUser2.setLogin("user2");
        testUser2.setName("User Two");
        testUser2.setBirthday(LocalDate.of(1985, 5, 5));
        testUser2.setFriends(new HashSet<>());
    }

    @Test
    public void testCreateUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(response, User.class);

        assertNotNull(createdUser.getId());
        assertEquals("test1@example.com", createdUser.getEmail());
        assertEquals("user1", createdUser.getLogin());
    }

    @Test
    public void testCreateUserWithDuplicateEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Этот имейл уже используется"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isOk())
                .andReturn();

        User createdUser = objectMapper.readValue(createResult.getResponse().getContentAsString(), User.class);

        createdUser.setName("Updated Name");
        createdUser.setLogin("updatedLogin");

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.login").value("updatedLogin"));
    }

    @Test
    public void testAddFriend() throws Exception {
        MvcResult createUser1Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult createUser2Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser2)))
                .andExpect(status().isOk())
                .andReturn();

        User user1 = objectMapper.readValue(createUser1Result.getResponse().getContentAsString(), User.class);
        User user2 = objectMapper.readValue(createUser2Result.getResponse().getContentAsString(), User.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(user2.getId()));
    }

    @Test
    public void testRemoveFriend() throws Exception {
        MvcResult createUser1Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult createUser2Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser2)))
                .andExpect(status().isOk())
                .andReturn();

        User user1 = objectMapper.readValue(createUser1Result.getResponse().getContentAsString(), User.class);
        User user2 = objectMapper.readValue(createUser2Result.getResponse().getContentAsString(), User.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testGetCommonFriends() throws Exception {
        MvcResult createUser1Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser1)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult createUser2Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser2)))
                .andExpect(status().isOk())
                .andReturn();

        User user3 = new User();
        user3.setEmail("test3@example.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1980, 10, 10));

        MvcResult createUser3Result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andExpect(status().isOk())
                .andReturn();

        User user1 = objectMapper.readValue(createUser1Result.getResponse().getContentAsString(), User.class);
        User user2 = objectMapper.readValue(createUser2Result.getResponse().getContentAsString(), User.class);
        User user3Created = objectMapper.readValue(createUser3Result.getResponse().getContentAsString(), User.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1.getId(), user3Created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user2.getId(), user3Created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(user3Created.getId()));
    }
}