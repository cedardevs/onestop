package ncei.onestop.api.metadata.controller

import groovy.util.logging.Slf4j
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Slf4j
@ControllerAdvice
class MetadataGlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

    if (status.is5xxServerError()) {
      log.error(ex)
    }

    def result = [
        errors: [
            [
                status: status.value() as String,
                title : buildTitle(status),
                detail: buildDetail(status, ex)
            ]
        ],
        meta: [
            timestamp: System.currentTimeMillis(),
            request: request?.getDescription(false),
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
