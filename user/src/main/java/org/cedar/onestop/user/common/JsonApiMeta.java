package org.cedar.onestop.user.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary copy of what should eventually exist in array-iteration module.
 */
public class JsonApiMeta {
  Map nonStandardMetadata;

  public JsonApiMeta(Map nonStandardMetadata) {
    this.nonStandardMetadata = nonStandardMetadata;
  }

  public Map getNonStandardMetadata() {
    return nonStandardMetadata;
  }

  static public class Builder {
    Map nonStandardMetadata = new HashMap();

    public Builder setNonStandardMetadata(Map nonStandardMetadata) {
      this.nonStandardMetadata = nonStandardMetadata;
      return this;
    }

    public JsonApiMeta build() {
      return new JsonApiMeta(this.nonStandardMetadata);
    }
  }
}
