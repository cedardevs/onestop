package org.cedar.onestop.api.metadata

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

// Reference:
// https://stackoverflow.com/questions/36387265/disable-all-database-related-auto-configuration-in-spring-boot#answer-55591954

@Configuration
@Import([WithDB.class, WithoutDB.class])
class DatasourceConfig {

  // when 'icam' profile is NOT active, we want to disable the
  // dependency-based datasource auto-configuration
  @Profile("!icam")
  @EnableAutoConfiguration(
      exclude = [
        DataSourceAutoConfiguration.class ,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
      ])
  static class WithoutDB {

  }

  // when 'icam' IS active, we want to enable the
  // default datasource auto-configuration
  @Profile("icam")
  @EnableAutoConfiguration
  static class WithDB {

  }
}