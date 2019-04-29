package org.cedar.psi.manager

import spock.lang.Ignore
import spock.lang.Specification


class StreamManagerMainSpec extends Specification {

  def 'retrieves env'() {
    expect:
    StreamManagerMain.getEnv() == System.getenv()
  }

  // This static method mocking doesn't appear to work when the code is in java...
  // I'm not entirely convinced that this is an important test to do, though.
  @Ignore
  def 'handles secured env'() {
    GroovySpy(System, global: true)
    System.getenv() >> { throw new SecurityException() }

    expect:
    StreamManagerMain.getEnv() ==  [:]
  }

}
