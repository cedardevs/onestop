package org.cedar.psi.manager

import spock.lang.Specification


class StreamManagerMainSpec extends Specification {

  def 'retrieves env'() {
    expect:
    StreamManagerMain.getEnv() ==  System.getenv()
  }

  def 'handles secured env'() {
    GroovySpy(System, global: true)
    System.getenv() >> { throw new SecurityException() }

    expect:
    StreamManagerMain.getEnv() ==  [:]
  }

}
