package com.inditex.prices.infrastructure.adapter.in.rest;

import com.inditex.prices.domain.exception.PriceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a Problem Details (RFC 9457). Los errores de binding y
 * validación (400) ya los emite la clase base con ese formato.
 */
@Slf4j
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String PRICE_NOT_FOUND_TITLE = "Price not found";
    private static final String INVALID_PARAMETERS_TITLE = "Invalid request parameters";
    private static final String INTERNAL_ERROR_TITLE = "Internal error";
    private static final String INTERNAL_ERROR_DETAIL = "Se ha producido un error inesperado";

    @ExceptionHandler(PriceNotFoundException.class)
    ProblemDetail onPriceNotFound(PriceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(PRICE_NOT_FOUND_TITLE);
        return problem;
    }

    /** Violaciones de las constraints del contrato en parámetros (vía @Validated). */
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail onConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(INVALID_PARAMETERS_TITLE);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail onUnexpected(Exception ex) {
        log.error(INTERNAL_ERROR_DETAIL, ex);
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_DETAIL);
        problem.setTitle(INTERNAL_ERROR_TITLE);
        return problem;
    }
}
