package ru.practicum.ewm.error;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.MissingFormatArgumentException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingFormatArgumentException.class,
            MissingRequestHeaderException.class,
            ValidationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final Exception e) {
        log.error(e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "Validation exception: {}", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final BadRequestException e) {
        log.error(e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "Bad request exception: {}", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.error(sw.toString());
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: {}", sw.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateTimeException(final DateTimeException e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.error(sw.toString());
        return new ApiError(HttpStatus.BAD_REQUEST, "Incorrect DateTime parameters: {}", sw.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.error(sw.toString());
        return new ApiError(HttpStatus.BAD_REQUEST, "Bad request exception:", sw.toString());
    }

}