package ncei.onestop.api.controller

import groovy.util.logging.Slf4j
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

import javax.servlet.http.HttpServletRequest

@Slf4j
@ControllerAdvice
class GlobalDefaultExceptionHandler {

  @ExceptionHandler(value = Exception)
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody Map defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
    // If annotated with @ResponseStatus rethrow it so the framework can handle it
    if (AnnotationUtils.findAnnotation(e.class, ResponseStatus)) {
      throw e
    }

    log.error("An exception occurred while handling a request to [${req.requestURL}]:", e)

    return [
        errors: [
            [
                status: "500",
                title : "Sorry, something has gone wrong.",
                detail: "Looks like something isn't working on our end, please try again later."
            ]
        ]
    ]
  }

}
