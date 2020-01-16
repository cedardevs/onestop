package org.cedar.onestop.elastic.common;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
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
import java.security.cert.X509Certificate;
import java.util.List;

public class ElasticsearchClient {
  private static final Logger log = LoggerFactory.getLogger(DocumentUtil.class);

  public static RestHighLevelClient create(List<String> elasticHosts, int elasticPort, boolean sslEnabled, String user, String pass) {
    HttpHost[] hosts = elasticHosts
        .stream()
        .map(host -> new HttpHost(host, elasticPort, sslEnabled ? "https" : "http"))
        .toArray(HttpHost[]::new);

    RestClientBuilder restClientBuilder = RestClient.builder(hosts)
        .setRequestConfigCallback(requestConfigBuilder -> {
          // Set connect timeout to 1 minute and socket timeout to 5 minutes
          return requestConfigBuilder.setConnectTimeout(60000).setSocketTimeout(300000);
        })
        .setHttpClientConfigCallback(httpClientBuilder -> {
          if (user != null && pass != null) {

            // credentials provider
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(user, pass);
            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);

            // hostname verifier
            // TODO
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

            // ssl context
            // TODO
            SSLContext sslContext = null;
            try {
              sslContext = new SSLContextBuilder()
                  .loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType) {
                      return true;
                    }
                  })
                  .build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
              e.printStackTrace();
            }

            httpClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)
                // TODO
                .setSSLHostnameVerifier(hostnameVerifier)
                .setSSLContext(sslContext);
          }
          // causes the builder to take system properties into account when building the
          // default ssl context, e.g. javax.net.ssl.trustStore, etc.
          httpClientBuilder.useSystemProperties();
          return httpClientBuilder;
        });

    return new RestHighLevelClient(restClientBuilder);
  }
}
