package org.cedar.onestop.elastic.common

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.testcontainers.elasticsearch.ElasticsearchContainer

class ElasticsearchTestContainer extends ElasticsearchContainer {

    // Client that connects to an Elasticsearch cluster through HTTP
    RestClient restClient

    ElasticsearchTestContainer(String dockerImageName) {
        super(dockerImageName)
        // set ES heap size ("-Xms1g -Xmx1g" is default)
        withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
        // disable security for tests so rest client does not need to pass credentials
        withEnv("xpack.security.enabled", "false")
    }

    @Override
    void start() {
        super.start()
        // setup HTTP rest client
        HttpHost host = HttpHost.create(this.getHttpHostAddress())
        restClient = RestClient.builder(host).build()
    }

    @Override
    void close() {
        super.close()
    }
}