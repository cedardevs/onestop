package ncei.onestop.api

import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

public class TestResponseErrorHandler implements ResponseErrorHandler {
  // Override the default exception handler so tests can see errors.
  @Override
  public void handleError(ClientHttpResponse clienthttpresponse) throws IOException {
      println("handleError:${clienthttpresponse.statusCode}:${clienthttpresponse.statusText}")
      // Return so test can see error.
  }

  @Override
  public boolean hasError(ClientHttpResponse clienthttpresponse) throws IOException {

    if (clienthttpresponse.getStatusCode() != HttpStatus.OK) {
      println("Status code: " + clienthttpresponse.getStatusCode())
      println("Response: " + clienthttpresponse.getStatusText())
      println(clienthttpresponse.getBody())
    }
    return false
  }
}