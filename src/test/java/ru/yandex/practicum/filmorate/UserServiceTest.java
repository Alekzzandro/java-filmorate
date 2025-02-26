package ru.yandex.practicum.filmorate;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testAddUser() throws ValidationException, DuplicatedDataException {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User addedUser = userService.addUser(user);

        assertNotNull(addedUser, "Пользователь не добавлен");
        assertEquals(user.getEmail(), addedUser.getEmail(), "Email пользователя не совпадает");
        assertEquals(user.getLogin(), addedUser.getLogin(), "Логин пользователя не совпадает");
        assertEquals(user.getName(), addedUser.getName(), "Имя пользователя не совпадает");
        assertEquals(user.getBirthday(), addedUser.getBirthday(), "Дата рождения пользователя не совпадает");
    }

    @Test
    public void testAddUserEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.addUser(user);
        });

        assertEquals("Имейл должен быть указан и содержать символ @", exception.getMessage());
    }

    @Test
    public void testAddUserInvalidEmail() {
        User user = new User();
        user.setEmail("testexample.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.addUser(user);
        });

        assertEquals("Имейл должен быть указан и содержать символ @", exception.getMessage());
    }

    @Test
    public void testAddUserEmptyLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.addUser(user);
        });

        assertEquals("Логин не может быть пустым и должен содержать пробелы", exception.getMessage());
    }

    @Test
    public void testAddUserLoginWithSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.addUser(user);
        });

        assertEquals("Логин не может быть пустым и должен содержать пробелы", exception.getMessage());
    }

    @Test
    public void testAddUserFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2030, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.addUser(user);
        });

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    public void testAddUserDuplicateEmail() throws ValidationException, DuplicatedDataException {
        User user1 = new User();
        user1.setEmail("test@example.com");
        user1.setLogin("testlogin1");
        user1.setName("Test Name 1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        userService.addUser(user1);

        User user2 = new User();
        user2.setEmail("test@example.com");
        user2.setLogin("testlogin2");
        user2.setName("Test Name 2");
        user2.setBirthday(LocalDate.of(2001, 1, 1));

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class, () -> {
            userService.addUser(user2);
        });

        assertEquals("Этот имейл уже используется", exception.getMessage());
    }

    @Test
    public void testUpdateUser() throws ValidationException, DuplicatedDataException {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userService.addUser(user);

        user.setName("Updated Name");
        user.setEmail("updated@example.com");

        User updatedUser = userService.updateUser(user);

        assertNotNull(updatedUser, "Пользователь не обновлен");
        assertEquals(user.getEmail(), updatedUser.getEmail(), "Email пользователя не совпадает");
        assertEquals(user.getLogin(), updatedUser.getLogin(), "Логин пользователя не совпадает");
        assertEquals(user.getName(), updatedUser.getName(), "Имя пользователя не совпадает");
        assertEquals(user.getBirthday(), updatedUser.getBirthday(), "Дата рождения пользователя не совпадает");
    }
}
