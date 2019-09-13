package org.cedar.onestop.api.admin

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

// only use this auto configuration when icam feature/profile is enabled
@Profile("icam")
@Configuration
class DataSourceAutoConfigurationConditional extends DataSourceAutoConfiguration { }