package com.demo.weatherapi.exception;

import com.demo.weatherapi.dto.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage()).toList();

        ErrorResponse response = new ErrorResponse(
                "Ошибки валидации данных прогноза",
                HttpStatus.BAD_REQUEST.value(),
                errorMessages
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<String> errors = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        return buildResponse(errors);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException ex) {
        return buildResponse(
                "Invalid date format. Expected format: yyyy-MM-dd", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String parameter = ex.getName();
        Object value = ex.getValue();
        Class<?> requiredType = ex.getRequiredType();

        Map<Class<?>, String> expectedFormats = Map.of(
                java.time.LocalDate.class, "yyyy-MM-dd",
                Long.class, "целое число (например, 123)",
                Integer.class, "целое число (например, 42)"
        );

        String expected = expectedFormats.getOrDefault(requiredType,
                requiredType != null ? requiredType.getSimpleName() : "unknown");

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected format: %s",
                value, parameter, expected
        );

        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        String message = "Ошибка в формате тела запроса";

        if (cause instanceof UnrecognizedPropertyException upe) {
            List<String> unknownFields = upe.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .toList();

            message = "Неизвестные поля в теле запроса: " + String.join(", ", unknownFields);
        }

        ErrorResponse error = new ErrorResponse(
                message,
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(message, status.value()), status);
    }

    private ResponseEntity<ErrorResponse> buildResponse(List<String> validationErrors) {
        return new ResponseEntity<>(new ErrorResponse("Constraint violation",
                HttpStatus.BAD_REQUEST.value(), validationErrors), HttpStatus.BAD_REQUEST);
    }
}