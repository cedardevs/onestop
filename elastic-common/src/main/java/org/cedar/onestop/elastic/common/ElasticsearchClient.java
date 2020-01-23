package org.cedar.onestop.elastic.common;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ElasticsearchClient {
  private static final Logger log = LoggerFactory.getLogger(DocumentUtil.class);

  public static RestHighLevelClient create(List<String> elasticHosts, int elasticPort, boolean sslEnabled, String user, String pass) {

    String scheme = sslEnabled ? "https" : "http";

    // map the 
    HttpHost[] hosts = elasticHosts
        .stream()
        .map(host -> new HttpHost(host, elasticPort, scheme))
        .toArray(HttpHost[]::new);

    final CredentialsProvider credentialsProvider =
        new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(user, pass));


    RestClientBuilder restClientBuilder = RestClient.builder(hosts)
        .setHttpClientConfigCallback(httpClientBuilder -> {
          httpClientBuilder
              .setDefaultCredentialsProvider(credentialsProvider);
          if(sslEnabled) {

            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLContext sslContext = null;
            try {
              sslContext = new SSLContextBuilder()
                  .loadTrustMaterial(null, (chain, authType) -> {
                    return true;
                  })
                  .build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
              e.printStackTrace();
            }
            httpClientBuilder
                .setSSLHostnameVerifier(hostnameVerifier)
                .setSSLContext(sslContext);
          }
          return httpClientBuilder;
        });

    return new RestHighLevelClient(restClientBuilder);
  }
}
