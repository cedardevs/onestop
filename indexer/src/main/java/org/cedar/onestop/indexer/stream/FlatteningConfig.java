package org.cedar.onestop.indexer.stream;

import org.cedar.onestop.data.util.FileUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

public class FlatteningConfig {

  private final Boolean enabled;
  private final String storeName;
  private final String script;
  private final Duration interval;

  private FlatteningConfig(Boolean enabled, String storeName, String script, Duration interval) {
    this.enabled = enabled != null ? enabled : true; // enabled by default
    this.storeName = storeName;
    this.script = script;
    this.interval = interval;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public String getStoreName() {
    return storeName;
  }

  public String getScript() {
    return script;
  }

  public Duration getInterval() {
    return interval;
  }

  public static FlatteningConfigBuilder newBuilder() {
    return new FlatteningConfigBuilder();
  }

  public static class FlatteningConfigBuilder {
    private Boolean enabled;
    private String storeName;
    private String scriptPath;
    private Duration interval;

    FlatteningConfigBuilder() {}

    public FlatteningConfigBuilder withEnabled(Boolean bool) {
      enabled = bool;
      return this;
    }

    public FlatteningConfigBuilder withStoreName(String name) {
      storeName = name;
      return this;
    }

    public FlatteningConfigBuilder withScriptPath(String path) {
      scriptPath = path;
      return this;
    }

    public FlatteningConfigBuilder withInterval(Duration duration) {
      interval = duration;
      return this;
    }

    public FlatteningConfig build() {
      Objects.requireNonNull(storeName, "storeName is required");
      Objects.requireNonNull(scriptPath, "scriptPath is required");
      Objects.requireNonNull(interval, "interval is required");
      return new FlatteningConfig(enabled, storeName, getScriptText(), interval);
    }

    private String getScriptText() {
      try {
        var flatteningScript = FileUtils.textFromClasspathFile(scriptPath);
        if (flatteningScript == null || flatteningScript.isEmpty()) {
          throw new IllegalStateException("Required flattening script [" + scriptPath + "] is empty");
        }
        else {
          return flatteningScript;
        }
      }
      catch(IOException e) {
        throw new IllegalStateException("Failed to load required flattening script from [" + scriptPath + "]", e);
      }
    }
  }


}
