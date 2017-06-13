package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.elasticsearch.ElasticsearchException


@Slf4j
@ControllerAdvice
class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = [Exception.class])
  protected ResponseEntity<Object> handleExceptions(Exception ex, WebRequest request) {

    def status, title, detail
    if (ex instanceof ElasticsearchException) {
      status = ex.status().getStatus()

      if (status >= 400 && status < 500) {
        title = 'Request Parsing Error'
        detail = 'There was an error with your request, please revise and try again.'
      }
      if (status >= 500) {
        title = 'Internal Error'
        detail = 'There was an error on our end, please try again later.'
      }
    }

    // FIXME This should be more accurate for non-ES exceptions:
    def result = [
        errors: [
            [
                status: status ?: 400,
                title : title ?: "Sorry, something has gone wrong",
                detail: detail ?: "Looks like something isn't working right now, please try again later"
            ]
        ],
        meta: [
            timestamp: System.currentTimeMillis(),
            request: request?.getDescription(false),
            parameters: request?.parameterMap
        ]
    ]

    return handleExceptionInternal(ex, result, new HttpHeaders(), HttpStatus.valueOf(status), request)
  }

}
