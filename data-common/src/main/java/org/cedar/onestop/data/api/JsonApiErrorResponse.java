package org.cedar.onestop.data.api;

import java.util.List;

public class JsonApiErrorResponse extends JsonApiResponse {

  // There are lots other optional fields that can be added if needed.

  private JsonApiErrorResponse(List<JsonApiError> errors, JsonApiMeta meta) {
    this.errors = errors;
    this.meta = meta;
  }

  public List<JsonApiError> getErrors() {
    return errors;
  }

  public JsonApiMeta getMeta() {
    return meta;
  }

  static public class Builder {
    List<JsonApiError> errors;
    JsonApiMeta meta;
    // There are lots other optional fields that can be added if needed.

    public JsonApiErrorResponse.Builder setErrors(List<JsonApiError> errors) {
      this.errors = errors;
      return this;
    }

    public JsonApiErrorResponse.Builder setMeta(JsonApiMeta meta) {
      this.meta = meta;
      return this;
    }

    public JsonApiErrorResponse build() {
      if (errors != null || meta != null) {
        return new JsonApiErrorResponse(errors, meta);
      } else {
        throw new NullPointerException("JSON:Error response must have either errors or meta set");
      }
    }
  }

}
