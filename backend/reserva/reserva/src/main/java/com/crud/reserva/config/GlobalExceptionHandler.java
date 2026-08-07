package com.crud.reserva.config;

import com.crud.reserva.DTO.ErrorResponseDTO;
import com.crud.reserva.exception.ReservasNaoEncontradasException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> erros = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(erro -> erros.put(
                        erro.getField(),
                        erro.getDefaultMessage()
                ));

        return build(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Erro de validacao",
                request.getRequestURI(),
                erros
        );
    }

    @ExceptionHandler(ReservasNaoEncontradasException.class)
    public ResponseEntity<ErrorResponseDTO> handleReservasNaoEncontradas(
            ReservasNaoEncontradasException ex,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataInvalida(
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                "Data invalida. Use o formato YYYY-MM-DD.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        return build(
                status,
                ex.getReason(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor",
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ErrorResponseDTO> build(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponseDTO(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        path,
                        errors
                ));
    }
}
