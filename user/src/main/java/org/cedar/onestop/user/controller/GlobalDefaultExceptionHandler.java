package org.cedar.onestop.user.controller;

import org.cedar.onestop.data.api.*;
import org.cedar.onestop.user.service.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public JsonApiResponse handleResourceNotFoundException(ResourceNotFoundException ex,
                                                         HttpServletResponse response) {
        return new JsonApiErrorResponse.Builder()
          .setTitle("Resource not found. Exception: "+ex.getMessage())
          .setStatus(HttpStatus.BAD_REQUEST.value(),response)
          .setCode("No resource for that id")
          .build();
  }

  @ExceptionHandler(AccessDeniedException.class)
  public JsonApiResponse handleAccessDeniedException(AccessDeniedException ex,
                                                     HttpServletResponse response) {
    return new JsonApiErrorResponse.Builder()
      .setTitle("You do not have permission to access this resource. Exception: "+ex.getMessage())
      .setStatus(HttpStatus.FORBIDDEN.value(), response)
      .setCode("No resource for that id")
      .build();
  }

  @ExceptionHandler(Exception.class)
  public JsonApiResponse handleException (Exception ex,
                                          HttpServletResponse response) {
    return new JsonApiErrorResponse.Builder()
      .setTitle("Exception: " + ex.getMessage())
      .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value(), response)
      .setCode("Unknown error happened. Error: " + ex.getMessage())
      .setDetail("Exception: " + ex.getMessage())
      .build();
  }
}
