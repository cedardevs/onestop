package org.cedar.onestop.indexer.stream;

import org.cedar.onestop.elastic.common.FileUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

public class FlatteningConfig {

  private final String storeName;
  private final String script;
  private final Duration interval;

  private FlatteningConfig(String storeName, String script, Duration interval) {
    this.storeName = storeName;
    this.script = script;
    this.interval = interval;
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
    private String storeName;
    private String scriptPath;
    private Duration interval;

    FlatteningConfigBuilder() {}

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
      return new FlatteningConfig(storeName, getScriptText(), interval);
    }

    private String getScriptText() {
      try {
        var flatteningScript = FileUtil.textFromClasspathFile(scriptPath);
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
