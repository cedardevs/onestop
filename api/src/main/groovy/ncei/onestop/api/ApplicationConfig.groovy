package ncei.onestop.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient

@Configuration
class ApplicationConfig {

    @Bean
    //@Profile("dev") TODO beans defined for dev/test/prod?
    public Client transportClient() {
        def client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))

        client
    }
}

