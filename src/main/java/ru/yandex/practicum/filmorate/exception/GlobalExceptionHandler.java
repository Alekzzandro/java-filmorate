package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(ValidationException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleException(Exception ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", "Произошла внутренняя ошибка сервера");
    return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DuplicatedDataException.class)
  public ResponseEntity<Map<String, String>> handleDuplicatedDataException(DuplicatedDataException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }
}