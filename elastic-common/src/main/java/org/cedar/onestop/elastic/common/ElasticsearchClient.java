package org.cedar.onestop.elastic.common;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class ElasticsearchClient {
  private static final Logger log = LoggerFactory.getLogger(DocumentUtil.class);

  private static SSLContext createSslContext(String certFilePath)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException, UnrecoverableKeyException {

    final File certFile = new File(certFilePath);
    final FileInputStream certStream = new FileInputStream(certFile);

    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(certStream);
    String alias = cert.getSubjectX500Principal().getName();

    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null);
    trustStore.setCertificateEntry(alias, cert);
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(trustStore, null);
    KeyManager[] keyManagers = kmf.getKeyManagers();

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
    tmf.init(trustStore);
    TrustManager[] trustManagers = tmf.getTrustManagers();

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyManagers, trustManagers, null);

    return sslContext;
  }

  public static RestHighLevelClient create(List<String> elasticHosts, int elasticPort, boolean sslEnabled, String certFilePath, String user, String pass) {

    String scheme = sslEnabled ? "https" : "http";

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
            SSLContext sslContext = null;
            try {
              sslContext = createSslContext(certFilePath);
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException e) {
              e.printStackTrace();
            }
            httpClientBuilder
                .setSSLContext(sslContext);
          }
          return httpClientBuilder;
        });

    return new RestHighLevelClient(restClientBuilder);
  }
}
