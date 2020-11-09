package org.cedar.onestop.gateway;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class GatewayApplicationTests {
	@Autowired
	private ConfigurableApplicationContext	context;

	@Test
	void contextLoads() {
		assertTrue(context.isActive());
	}
}
