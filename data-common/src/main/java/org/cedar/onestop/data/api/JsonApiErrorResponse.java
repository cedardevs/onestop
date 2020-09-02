package org.cedar.onestop.data.api;

public class JsonApiErrorResponse extends JsonApiResponse {

  String id;
  int status;
  String code;
  String title;
  String detail;
  JsonApiMeta meta;
  ErrorSource source;
  // There are lots other optional fields that can be added if needed.

  public JsonApiErrorResponse(String id, int status, String code, String title, String detail, JsonApiMeta meta, ErrorSource source) {
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

  public int getStatus() {
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
    int status;
    String code;
    String title;
    String detail;
    JsonApiMeta meta;
    ErrorSource source;

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    /**
     * The HTTP status code applicable to this problem.
     * @param status HttpStatus status code
     * @return
     */
    public Builder setStatus(int status) {
      this.status = status;
      return this;
    }

    /**
     * An application-specific error code, expressed as a string value.
     * @param code String application-specific error code
     * @return
     */
    public Builder setCode(String code) {
      this.code = code;
      return this;
    }

    /**
     * A short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization.
     * @param title String
     * @return
     */
    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    /**
     * A human-readable explanation specific to this occurrence of the problem. Like title, this field’s value can be localized.
     * @param detail String
     * @return
     */
    public Builder setDetail(String detail) {
      this.detail = detail;
      return this;
    }

    /**
     * A meta object containing non-standard meta-information about the error.
     * @param meta JsonApiMeta A meta object containing non-standard meta-information about the error.
     * @return
     */
    public Builder setMeta(JsonApiMeta meta) {
      this.meta = meta;
      return this;
    }

    /**
     * An object containing references to the source of the error,
     * @param source ErrorSource An object containing references to the source of the error,
     * @return
     */
    public Builder setSource(ErrorSource source) {
      this.source = source;
      return this;
    }

    public JsonApiErrorResponse build() {
      return new JsonApiErrorResponse(this.id, this.status, this.code, this.title, this.detail, this.meta,this.source);
    }
  }
}