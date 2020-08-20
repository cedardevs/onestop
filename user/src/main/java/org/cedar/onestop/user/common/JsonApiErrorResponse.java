package org.cedar.onestop.user.common;

import org.springframework.http.HttpStatus;

/**
 * Temporary copy of what should eventually exist in array-iteration module.
 */

public class JsonApiErrorResponse extends JsonApiResponse {

  String id;
  //  JsonLinks links;
  HttpStatus status;
  String code;
  String title;
  String detail;
  JsonApiMeta meta;
  ErrorSource source;

  public JsonApiErrorResponse(String id, HttpStatus status, String code, String title, String detail, JsonApiMeta meta, ErrorSource source) {
    this.id = id;
    this.status = status;
    this.code = code;
    this.title = title;
    this.detail = detail;
    this.source = source;
    this.meta = meta;
  }

  public String getId() {
    return id;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }

  public JsonApiMeta getMeta() {
    return meta;
  }

  public ErrorSource getSource() {
    return source;
  }

  private class ErrorSource {
    String pointer;
    String parameter;
  }

  static public class Builder {

    String id;
    //  JsonLinks links;
    HttpStatus status;
    String code;
    String title;
    String detail;
    JsonApiMeta meta;
    ErrorSource source;

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setStatus(HttpStatus status) {
      this.status = status;
      return this;
    }

    public Builder setCode(String code) {
      this.code = code;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setDetail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder setMeta(JsonApiMeta meta) {
      this.meta = meta;
      return this;
    }

    public Builder setSource(ErrorSource source) {
      this.source = source;
      return this;
    }

    public JsonApiErrorResponse build() {
      return new JsonApiErrorResponse(this.id, this.status, this.code, this.title, this.detail, this.meta,this.source);
    }
  }
}