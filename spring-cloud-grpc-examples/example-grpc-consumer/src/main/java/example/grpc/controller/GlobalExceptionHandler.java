package example.grpc.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

/**
 * @author icodening
 * @date 2022.07.24
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public Object handler(Throwable throwable) {
        return Collections.singletonMap("message", throwable.getMessage());
    }
}
