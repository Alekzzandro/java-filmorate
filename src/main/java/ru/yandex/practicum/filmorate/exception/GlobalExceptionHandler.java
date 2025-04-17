package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(FilmNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFilmNotFoundException(FilmNotFoundException e) {
        return new ErrorResponse("Фильм не найден", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ErrorResponse("Ошибка валидации", errorMessage);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomValidationException(ValidationException e) {
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
        return new ErrorResponse("Пользователь не найден", e.getMessage());
    }

    @ExceptionHandler(InvalidReleaseDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidReleaseDateException(InvalidReleaseDateException ex) {
        return new ErrorResponse("Ошибка валидации", ex.getMessage());
    }

    @ExceptionHandler(MpaNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMpaNotFoundException(MpaNotFoundException ex) {
        return new ErrorResponse("Ошибка валидации", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherExceptions(Exception e) {
        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
    }
}