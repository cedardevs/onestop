package org.cedar.onestop.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("gateway")
public class GatewayConfig {

  private Map<String, String> routes = new HashMap<>();

  public Map<String, String> getRoutes() {
    return routes;
  }

  public String backend(String service) {
    return routes.get(service);
  }
}
