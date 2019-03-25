package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.authorization.configs.SpringSecurityConfig
import org.cedar.onestop.api.metadata.authorization.configs.SpringSecurityDisabled
import org.cedar.onestop.api.metadata.springsecurity.IdentityProviderConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@ActiveProfiles("integration")
@SpringBootTest(
        classes = [
                Application,
                IntegrationTestConfig,
                SpringSecurityDisabled,
                SpringSecurityConfig,
                IdentityProviderConfig
        ],
        webEnvironment = RANDOM_PORT
)
@Unroll
class IntegrationTest extends Specification {
}