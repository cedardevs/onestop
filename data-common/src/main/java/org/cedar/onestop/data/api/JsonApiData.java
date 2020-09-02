package org.cedar.onestop.data.api;

import java.util.*;

public class JsonApiData {
  private String id;
  private String type;
  private Map<String, Object> attributes;

  private JsonApiData(String id, String type, Map<String, Object> attributes) {
    this.id = id;
    this.type = type;
    this.attributes = attributes;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public static class Builder {
    private String id;
    private String type;
    private Map<String, Object> attributes;
    public Builder() {}

    public Builder setId(String id) {
      if(id == null || id.isBlank()) {
        throw new NullPointerException("JSON:API data element must have declared id");
      }
      this.id = id;
      return this;
    }

    public Builder setType(String type) {
      if(type == null || type.isBlank()) {
        throw new NullPointerException("JSON:API data element must have declared type");
      }
      this.type = type;
      return this;
    }

    public Builder setAttributes(Map<String, Object> attributes) {
      this.attributes = attributes;
      return this;
    }

    public JsonApiData build() {
      if(id != null && type != null) {
        return new JsonApiData(id, type, attributes);
      }
      else {
        throw new NullPointerException("JSON:API data element must have id, type, and attributes set");
      }
    }
  }
}