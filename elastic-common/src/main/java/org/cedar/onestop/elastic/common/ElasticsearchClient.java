package org.cedar.onestop.elastic.common;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class ElasticsearchClient {

  public static RestHighLevelClient create(List<String> elasticHosts, int elasticPort, boolean sslEnabled, String user, String pass) {
    HttpHost[] hosts = elasticHosts
        .stream()
        .map(host -> new HttpHost(host, elasticPort, sslEnabled ? "https" : "http"))
        .toArray(HttpHost[]::new);

    final CredentialsProvider credentialsProvider =
        new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(user, pass));

    HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

    SSLContext sslContext = null;
    try {
      sslContext = new SSLContextBuilder()
          .loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
              return true;
            }
          }).build();
    } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      e.printStackTrace();
    }

    SSLContext finalSslContext = sslContext;

    RestClientBuilder restClientBuilder = RestClient.builder(hosts)
        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
            .setDefaultCredentialsProvider(credentialsProvider)
            .setSSLHostnameVerifier(hostnameVerifier)
            .setSSLContext(finalSslContext));

    return new RestHighLevelClient(restClientBuilder);
  }
}
