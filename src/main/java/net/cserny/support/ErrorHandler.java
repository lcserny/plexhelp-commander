package net.cserny.support;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApplicationErrorResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorResponse> handleException(Exception e) {
        String stackTrace = ExceptionUtils.getStackTrace(e);
        log.error(stackTrace);

        return new ResponseEntity<>(ApplicationErrorResponse.builder()
                .type(e.getClass().getName())
                .message(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}
