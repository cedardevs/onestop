package org.cedar.onestop.user.common;

import java.util.*;

public class JsonApiSuccessResponse extends JsonApiResponse {

  List<JsonApiData> data;
  JsonApiMeta meta;
//  List<JsonApiLink> links; ?

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

  static public class Builder {
    List<JsonApiData> data;
    JsonApiMeta meta;

    public Builder setData(List<JsonApiData> data) {
      this.data = data;
      return this;
    }

    public Builder setMeta(JsonApiMeta meta) {
      this.meta = meta;
      return this;
    }

    public JsonApiSuccessResponse build() {
      if (data != null || meta != null) {
        return new JsonApiSuccessResponse(data, meta);
      } else {
        throw new NullPointerException("JSON:Successful response must have data or meta set");
      }
    }
  }
}
