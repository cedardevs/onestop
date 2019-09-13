package org.cedar.onestop.api.admin

import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class TestResponseErrorHandler implements ResponseErrorHandler {
  // Override the default exception handler so tests can see errors.
  @Override
  void handleError(ClientHttpResponse clienthttpresponse) throws IOException {
      println("handleError:${clienthttpresponse.statusCode}:${clienthttpresponse.statusText}")
      // Return so test can see error.
  }

  @Override
  boolean hasError(ClientHttpResponse clienthttpresponse) throws IOException {
    if (clienthttpresponse.getStatusCode() != HttpStatus.OK) {
      println("Status code: " + clienthttpresponse.getStatusCode())
      println("Response: " + clienthttpresponse.getStatusText())
      println(clienthttpresponse.getBody())
    }
    return false
  }
}
