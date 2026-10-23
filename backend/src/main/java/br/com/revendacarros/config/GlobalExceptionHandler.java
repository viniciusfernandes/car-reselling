package br.com.revendacarros.config;

import br.com.revendacarros.domain.exception.ConflictException;
import br.com.revendacarros.domain.exception.InvalidStateException;
import br.com.revendacarros.domain.exception.NotFoundException;
import br.com.revendacarros.infrastructure.storage.DocumentoStorageException;
import br.com.revendacarros.security.AuthTokenException;
import br.com.revendacarros.usecase.UseCaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .toList();
        log.warn("Falha de validação em {} {}: {}", request.getMethod(), request.getRequestURI(), errors);
        return ResponseEntity.badRequest().body(new ApiErrorResponse(errors, traceId(request)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex,
                                                             HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        log.warn("Violação de restrição em {} {}: {}", request.getMethod(), request.getRequestURI(), errors);
        return ResponseEntity.badRequest().body(new ApiErrorResponse(errors, traceId(request)));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        log.warn("Não encontrado em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(List.of(ex.getMessage()), traceId(request)));
    }

    @ExceptionHandler({ConflictException.class, InvalidStateException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        log.warn("Conflito em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(List.of(safeMessage(ex, "Conflito")), traceId(request)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                  HttpServletRequest request) {
        log.warn("Argumento inválido em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(List.of(safeMessage(ex, "Requisição inválida")), traceId(request)));
    }

    @ExceptionHandler(UseCaseException.class)
    public ResponseEntity<ApiErrorResponse> handleUseCase(UseCaseException ex, HttpServletRequest request) {
        log.warn("Erro de caso de uso em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(List.of(ex.getMessage()), traceId(request)));
    }

    @ExceptionHandler(AuthTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthTokenException(AuthTokenException ex,
                                                                     HttpServletRequest request) {
        log.warn("Erro de token de autenticação em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(List.of(safeMessage(ex, "Não autorizado")), traceId(request)));
    }

    @ExceptionHandler(DocumentoStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoStorage(DocumentoStorageException ex,
                                                                  HttpServletRequest request) {
        log.error("Erro de armazenamento de documento em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(List.of(safeMessage(ex, "Falha ao armazenar documento")), traceId(request)));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex,
                                                               HttpServletRequest request) {
        log.error("Estado inválido em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(List.of(safeMessage(ex, "Erro inesperado")), traceId(request)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                             HttpServletRequest request) {
        log.debug("Recurso estático não encontrado em {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(List.of("Recurso não encontrado"), traceId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Exceção não tratada em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(List.of("Erro inesperado"), traceId(request)));
    }

    private static String traceId(HttpServletRequest request) {
        Object traceId = request.getAttribute("traceId");
        return traceId == null ? null : traceId.toString();
    }

    private static String formatFieldError(FieldError error) {
        if (error.getDefaultMessage() == null) {
            return error.getField() + ": invalid";
        }
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private static String safeMessage(Throwable ex, String fallback) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }
}
