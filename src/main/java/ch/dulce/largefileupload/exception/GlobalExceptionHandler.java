package ch.dulce.largefileupload.exception;

import ch.dulce.largefileupload.controller.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequests(BadRequestException badRequestException, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), badRequestException.getMessage(),
        request.getDescription(false), "BAD REQUEST");
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}
