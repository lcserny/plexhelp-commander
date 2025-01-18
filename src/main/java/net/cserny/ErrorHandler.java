package net.cserny;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApplicationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorResponse> handleException(Exception e) {
        log.error(e.getMessage());

        return new ResponseEntity<>(ApplicationErrorResponse.builder()
                .error(e.getClass().getName())
                .message(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}
