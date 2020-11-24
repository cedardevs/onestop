package org.cedar.onestop.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GatewayConfigUtil {

  @Autowired
  RouteDefinitionLocator routeDefinitionLocator;

  Logger logger = LoggerFactory.getLogger(GatewayConfigUtil.class);

  private boolean filterSecureRoutes(RouteDefinition routeDefinition){
    return routeDefinition
      .getFilters()
      .stream()
      .anyMatch(filter -> filter.getName().equals("TokenRelay"));
  }

  private List<String> extractPath(RouteDefinition routeDefinition){
    return routeDefinition
      .getPredicates()
      .stream()
      .flatMap(predicate -> predicate.getArgs().values().stream())
      .collect(Collectors.toList());
  }

  public String[] parseSecurePaths(){
    return Objects.requireNonNull(routeDefinitionLocator.getRouteDefinitions()
      //filter out routes that dont have a token relay filter
      .filter(this::filterSecureRoutes)
      .map(this::extractPath)
      .blockFirst())
      .stream()
      .peek(secureRoute -> logger.info("Securing route: " + secureRoute))
      .toArray(String[]::new);
  }

}
