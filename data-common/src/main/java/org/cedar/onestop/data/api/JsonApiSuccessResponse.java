package org.cedar.onestop.data.api;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class JsonApiSuccessResponse extends JsonApiResponse {

  int status;
  // There are lots other optional fields that can be added if needed.

  private JsonApiSuccessResponse( List<JsonApiData> data, JsonApiMeta meta, int status) {
    this.data = data;
    this.meta = meta;
    this.status = status;
  }

  public List<JsonApiData> getData() {
    return data;
  }

  public JsonApiMeta getMeta() {
    return meta;
  }

  public int getStatus() {
    return status;
  }

  static public class Builder {
    List<JsonApiData> data;
    JsonApiMeta meta;
    int status;
    // There are lots other optional fields that can be added if needed.

    public Builder setData(List<JsonApiData> data) {
      this.data = data;
      return this;
    }

    public Builder setMeta(JsonApiMeta meta) {
      this.meta = meta;
      return this;
    }

    public Builder setStatus(int status, HttpServletResponse response) {
      this.status = status;
      response.setStatus(status);
      return this;
    }

    public JsonApiSuccessResponse build() {
      if (status != 0 && (data != null || meta != null)) {
        return new JsonApiSuccessResponse(data, meta, status);
      } else {
        throw new NullPointerException("JSON:Successful response must have status and either data or meta set");
      }
    }
  }
}
