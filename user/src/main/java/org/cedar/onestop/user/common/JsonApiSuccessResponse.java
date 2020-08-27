package org.cedar.onestop.user.common;

import org.springframework.http.HttpStatus;

import java.util.*;

public class JsonApiSuccessResponse extends JsonApiResponse {

  List<JsonApiData> data;
  JsonApiMeta meta;
  HttpStatus status;
  // There are lots other optional fields that can be added if needed.

  private JsonApiSuccessResponse( List<JsonApiData> data, JsonApiMeta meta) {
    this.data = data;
    this.meta = meta;
  }

  public List<JsonApiData> getData() {
    return data;
  }

  public JsonApiMeta getMeta() {
    return meta;
  }

  public HttpStatus getStatus() {
    return status;
  }

  static public class Builder {
    List<JsonApiData> data;
    JsonApiMeta meta;
    HttpStatus status;
    // There are lots other optional fields that can be added if needed.

    public Builder setData(List<JsonApiData> data) {
      this.data = data;
      return this;
    }

    public Builder setMeta(JsonApiMeta meta) {
      this.meta = meta;
      return this;
    }

    public Builder setStatus(HttpStatus status) {
      this.status = status;
      return this;
    }

    public JsonApiSuccessResponse build() {
      if (status != null && (data != null || meta != null)) {
        return new JsonApiSuccessResponse(data, meta);
      } else {
        throw new NullPointerException("JSON:Successful response must have status and either data or meta set");
      }
    }
  }
}
