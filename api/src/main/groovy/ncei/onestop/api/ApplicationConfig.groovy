package ncei.onestop.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient

@Configuration
class ApplicationConfig {

    @Value('${elasticsearch.port:9300}')
    Integer elasticPort

    @Value('${elasticsearch.host:localhost}')
    String elasticHost

    @Bean(destroyMethod = 'close')
    public Client transportClient() {
        TransportClient.builder().build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort))
    }

}

