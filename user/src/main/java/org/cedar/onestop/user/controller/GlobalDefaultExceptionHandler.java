package org.cedar.onestop.user.controller;

import org.apache.logging.log4j.util.Strings;
import org.cedar.onestop.data.api.JsonApiError;
import org.cedar.onestop.data.api.JsonApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;

@ControllerAdvice
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
    log.debug("Handling AccessDeniedException: {}", ex.getMessage());
    var status = request.getUserPrincipal() != null ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
    return this.handleExceptionInternal(ex, null, new HttpHeaders(), status, request);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleGeneralException(Exception ex, WebRequest request) {
    log.warn("Handling general exception", ex);
    return this.handleExceptionInternal(ex, null, new HttpHeaders(), getHttpStatus(ex), request);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
    var code = status.value();
    var title = getReason(ex);
    var jsonApiError = new JsonApiError.Builder()
        .setTitle(title)
        .setStatus(code)
        .setCode(String.valueOf(code))
        .setDetail(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
        .build();
    var jsonApiResponse = new JsonApiErrorResponse.Builder()
        .setErrors(Collections.singletonList(jsonApiError))
        .build();
    return super.handleExceptionInternal(ex, jsonApiResponse, headers, status, request);
  }

  /**
   * Extract the status from an exception based on {@link ResponseStatus} annotation if present,
   * or the code from a {@link HttpStatusCodeException} if it is an instance of that type, or
   * {@link HttpStatus#INTERNAL_SERVER_ERROR} by default.
   *
   * @param ex The throw exception
   * @return The {@link HttpStatus}
   */
  private static HttpStatus getHttpStatus(Exception ex) {
    // check for the ResponseStatus annotation and handle Spring's @AliasFor behavior
    var mergedAnnotation = AnnotatedElementUtils.getMergedAnnotation(ex.getClass(), ResponseStatus.class);
    if (mergedAnnotation != null) {
      var statusCode = mergedAnnotation.value();
      log.debug("Using status code " + statusCode + " from ResponseStatus annotation");
      return statusCode;
    }
    else if (ex instanceof HttpStatusCodeException) {
      var statusCode = ((HttpStatusCodeException) ex).getStatusCode();
      log.debug("Using status code " + statusCode + " from HttpStatusCodeException");
      return statusCode;
    }
    else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  /**
   * Extract the reason from an exception based on {@link ResponseStatus} annotation if present
   *
   * @param ex The throw exception
   * @return The reason string indicated in the {@link ResponseStatus} annotation,
   * else the exception's {@link Exception#getMessage() message}
   */
  private static String getReason(Exception ex) {
    var defaultReason = "An error occurred";
    if (ex.getClass().isAnnotationPresent(ResponseStatus.class)) {
      var annotatedReason = ex.getClass().getAnnotation(ResponseStatus.class).reason();
      return Strings.isBlank(annotatedReason) ? defaultReason : annotatedReason;
    }
    else {
      return Strings.isBlank(ex.getMessage()) ? defaultReason : ex.getMessage();
    }
  }
}
