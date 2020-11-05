package org.cedar.onestop.gateway;

import org.cedar.onestop.gateway.config.GatewayConfigUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest(classes = { GatewayApplication.class, GatewayConfigUtil.class})
public class GatewayConfigSpec {

  @Autowired
  GatewayConfigUtil configUtil;

  Logger logger = LoggerFactory.getLogger(GatewayConfigSpec.class);

  @Test
  void parseSecureRoutes(){
    //based on config in test/resources
    String[] secureRoutes = {"/api/service1/**"};
    logger.info(Arrays.toString(configUtil.parseSecurePaths()));
    assert configUtil.parseSecurePaths()[0].equals(secureRoutes[0]);
  }
}
