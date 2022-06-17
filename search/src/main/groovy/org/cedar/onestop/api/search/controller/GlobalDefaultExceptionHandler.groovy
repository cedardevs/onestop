package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import org.elasticsearch.client.ResponseException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Slf4j
@ControllerAdvice
class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = [Exception.class])
  protected ResponseEntity<Object> handleExceptions(Exception ex, WebRequest request) {
    int intStatus
    Map error = [:]

    log.error("Exception occurred: ", ex)

    if (ex instanceof ResponseException) {
      intStatus = ex.response.statusLine.statusCode
      error.status = intStatus

      if (intStatus >= 400 && intStatus < 500) {
        error.title = 'Request Parsing Error'
        error.detail = 'There was an error with your request, please revise and try again.'
      }
      if (intStatus >= 500) {
        error.title = 'Internal Error'
        error.detail = 'There was an error on our end, please try again later.'
      }
    }
    else {
      intStatus = 500
      error.status = 500
      error.title = 'Sorry, something has gone wrong'
      error.detail = 'Looks like something isn\'t working right now, please try again later'
    }

    def result = [
      errors: [error],
      meta: [
        timestamp: System.currentTimeMillis(),
        request: request?.getDescription(false),
        parameters: request?.parameterMap
      ]
    ]

    return super.handleExceptionInternal(ex, result, new HttpHeaders(), HttpStatus.valueOf(intStatus), request)
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

    if (status.is5xxServerError()) {
      log.error(ex.message)
    }

    def result = [
        errors: [
            [
                status: status.value() as String,
                title : buildTitle(status),
                detail: buildDetail(status, ex)
            ]
        ],
        meta  : [
            timestamp : System.currentTimeMillis(),
            request   : request?.getDescription(false),
            parameters: request?.parameterMap
        ]
    ]

    return super.handleExceptionInternal(ex, result, headers, status, request)
  }

  private static buildTitle(HttpStatus status) {
    status.is5xxServerError() ?
        "Sorry, something has gone wrong" :
        status.reasonPhrase
  }

  private static buildDetail(HttpStatus status, Exception e = null) {
    status.is5xxServerError() ?
        "Looks like something isn't working on our end, please try again later" :
        sanitizeExceptionMessage(e)
  }

  private static sanitizeExceptionMessage(Exception e) {
    def message = e?.message ?: 'Bad Request'
    return message.tokenize(':').first()
  }

}
