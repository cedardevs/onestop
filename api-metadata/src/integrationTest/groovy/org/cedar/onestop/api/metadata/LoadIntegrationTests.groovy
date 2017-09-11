package org.cedar.onestop.api.metadata

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Unroll
@ActiveProfiles("integration")
@SpringBootTest(classes = [Application, IntegrationTestConfig], webEnvironment = RANDOM_PORT)
class LoadIntegrationTests {

  /**
   * Tests should cover:
   *  - Load single record (confirm with GET)
   *  - Load multiple records (confirm with GET)
   *  - GET by ES id
   *  - GET by fileId; doi; fileId & doi
   *  - DELETE by ES id (recursive & not recursive)
   *  - DELETE by fileId; doi; fileId & doi (recursive & not recursive)
   *  - Verify error responses
   *
   * Anything in old tests related to what appears in Search index should really be tested in ETL
   */
}
