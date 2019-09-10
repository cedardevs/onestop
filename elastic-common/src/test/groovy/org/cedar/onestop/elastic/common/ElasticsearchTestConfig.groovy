package org.cedar.onestop.elastic.common

import groovy.util.logging.Slf4j
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile

@Slf4j
@Profile("integration")
@TestConfiguration
class ElasticsearchTestConfig {

    @Value('${elasticsearch.version}')
    String elasticsearchVersion

    // the `ci` (continuous integration) profile should not use a testcontainer beans to derive a RestClient,
    // as currently our CircleCI workflow can manage its own Elasticsearch docker image;
    // however, we still do need these testcontainer beans for running tests for local dev
    @Profile("!ci")
    @Bean(name = 'elasticsearchTestContainer', initMethod = 'start', destroyMethod = 'stop')
    ElasticsearchTestContainer elasticsearchTestContainer() {
        String dockerImageName = "docker.elastic.co/elasticsearch/elasticsearch:${elasticsearchVersion}"
        return new ElasticsearchTestContainer(dockerImageName)
    }
    @Profile("!ci")
    @Bean(name = 'restClient', destroyMethod = 'close')
    @DependsOn('elasticsearchTestContainer')
    RestClient elasticsearchRestClient() {
        ElasticsearchTestContainer container = elasticsearchTestContainer()
        RestClient restClient = container.restClient
        return restClient
    }
}
