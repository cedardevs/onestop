package org.cedar.onestop.elastic.common

import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile

@Profile("integration")
@TestConfiguration
class ElasticsearchTestConfig {

    @Value('${elasticsearch.version}')
    String elasticsearchVersion

    @Bean(name = 'elasticsearchTestContainer', initMethod = 'start', destroyMethod = 'stop')
    ElasticsearchTestContainer elasticsearchTestContainer() {
        String dockerImageName = "docker.elastic.co/elasticsearch/elasticsearch:${elasticsearchVersion}"
        return new ElasticsearchTestContainer(dockerImageName)
    }

    @Bean(name = 'elasticsearchRestClient', destroyMethod = 'close')
    @DependsOn('elasticsearchTestContainer')
    RestClient elasticsearchRestClient() {
        ElasticsearchTestContainer container = elasticsearchTestContainer()
        return container.restClient
    }

    ////
//    @Bean(name = 'elasticsearchConfig')
//    ElasticsearchConfig elasticsearchConfig() {
//        return new ElasticsearchConfig(
//                'search_collection',
//                'staging_collection',
//                'search_granule',
//                'staging_granule',
//                'search_flattened_granule',
//                'sitemap',
//                'prefix-',
//                'collection_pipeline',
//                'granule_pipeline',
//                'doc',
//                10,
//                null,
//                2,
//                5
//        )
//    }

}
