package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.controller.UserController;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserController userController;

    @BeforeEach
    public void setUp() {
        userController = new UserController();
    }

    @Test
    public void testAddUser() throws ValidationException, DuplicatedDataException {
        User user = new User(1, "test@example.com", "testlogin", "Test Name", LocalDate.of(2000, 1, 1));
        User addedUser = userController.create(user);

        assertNotNull(addedUser, "Пользователь не добавлен");
        assertEquals(user.getEmail(), addedUser.getEmail(), "Email пользователя не совпадает");
        assertEquals(user.getLogin(), addedUser.getLogin(), "Логин пользователя не совпадает");
        assertEquals(user.getName(), addedUser.getName(), "Имя пользователя не совпадает");
        assertEquals(user.getBirthday(), addedUser.getBirthday(), "Дата рождения пользователя не совпадает");
    }

    @Test
    public void testAddUserWithDuplicateEmail() throws ValidationException, DuplicatedDataException {
        User user1 = new User(1, "test@example.com", "login1", "User One", LocalDate.of(1995, 1, 1));
        userController.create(user1);

        User user2 = new User(2, "test@example.com", "login2", "User Two", LocalDate.of(1997, 1, 1));
        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class, () -> userController.create(user2));

        assertEquals("Этот имейл уже используется", exception.getMessage());
    }

    @Test
    public void testAddUserWithInvalidEmail() {
        User user = new User(1, "invalidemail.com", "testlogin", "Test Name", LocalDate.of(2000, 1, 1));
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Имейл должен быть указан и содержать символ @", exception.getMessage());
    }

    @Test
    public void testAddUserWithEmptyLogin() {
        User user = new User(1, "test@example.com", "", "Test Name", LocalDate.of(2000, 1, 1));
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    public void testAddUserWithFutureBirthday() {
        User user = new User(1, "test@example.com", "testlogin", "Test Name", LocalDate.of(2030, 1, 1));
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    public void testUpdateUser() throws ValidationException, DuplicatedDataException, ConditionsNotMetException {
        User user = new User(1, "test@example.com", "testlogin", "Test Name", LocalDate.of(2000, 1, 1));
        userController.create(user);

        User updatedUser = new User(1, "updated@example.com", "updatedlogin", "Updated Name", LocalDate.of(1995, 5, 5));
        User result = userController.update(updatedUser);

        assertNotNull(result, "Пользователь не обновлен");
        assertEquals(updatedUser.getEmail(), result.getEmail(), "Email пользователя не обновлен");
        assertEquals(updatedUser.getLogin(), result.getLogin(), "Логин пользователя не обновлен");
        assertEquals(updatedUser.getName(), result.getName(), "Имя пользователя не обновлено");
        assertEquals(updatedUser.getBirthday(), result.getBirthday(), "Дата рождения пользователя не обновлена");
    }
}
