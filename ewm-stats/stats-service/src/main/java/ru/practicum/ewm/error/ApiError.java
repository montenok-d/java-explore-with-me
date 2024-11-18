package ru.practicum.ewm.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ApiError {
    private HttpStatus status;
    private String errorMessage;
    private String terminalMessage;

    public ApiError(HttpStatus status, String errorMessage, String terminalMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.terminalMessage = terminalMessage;
    }
}
