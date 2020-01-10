package org.cedar.onestop.api.search;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.cedar.onestop.elastic.common.ElasticsearchCompatibility;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DefaultApplicationConfig {

  @Autowired
  Environment environment;

  @Value("${elasticsearch.port}")
  Integer elasticPort;

  @Value("#{'${elasticsearch.host:}'.split(',')}")
  List<String> elasticHost;

  @Value("${elasticsearch.ssl.enabled:}")
  Boolean sslEnabled;

  @Value("${elasticsearch.ro.user:}")
  String roUser;

  @Value("${elasticsearch.ro.pass:}")
  String roPassword;

  // default: null
  @Value("${elasticsearch.index.prefix:}")
  String PREFIX;
  // default: 10
  @Value("${elasticsearch.max-tasks:10}")
  Integer MAX_TASKS;
  // default: null
  @Value("${elasticsearch.requests-per-second:}")
  Integer REQUESTS_PER_SECOND;
  // optional: feature toggled by 'sitemap' profile, default: empty
  @Value("${etl.sitemap.scroll-size:}")
  Integer SITEMAP_SCROLL_SIZE;
  // optional: feature toggled by 'sitemap' profile, default: empty
  @Value("${etl.sitemap.collections-per-submap:}")
  Integer SITEMAP_COLLECTIONS_PER_SUBMAP;

  // we dont' want this bean to be created when the tests use test containers,
  // but in our CI environment, we have a separate Elasticsearch and so we can leverage this normal RestHighLevelClient again
  @Profile({"!integration", "ci"})
  @Bean(name = "restHighLevelClient", destroyMethod = "close")
  RestHighLevelClient restHighLevelClient() {
    HttpHost[] hosts = elasticHost
        .stream()
        .map(host -> new HttpHost(host, elasticPort, sslEnabled ? "https" : "http"))
        .toArray(HttpHost[]::new);
    RestClientBuilder restClientBuilder = RestClient.builder(hosts);

    restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
      if (roUser != null && roPassword != null) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(roUser, roPassword);
        credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }
      // causes the builder to take system properties into account when building the
      // default ssl context, e.g. javax.net.ssl.trustStore, etc.
      httpClientBuilder.useSystemProperties();
      return httpClientBuilder;
    });
    RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);

    // check for compatible elastic version (will throw exception if not compatible)
    ElasticsearchCompatibility.checkVersion(restHighLevelClient);

    return restHighLevelClient;
  }

  @Bean
  ElasticsearchConfig elasticsearchConfig() throws IOException {
    Boolean sitemapEnabled = Arrays.stream(environment.getActiveProfiles()).anyMatch(profile -> profile.compareTo("sitemap") == 0);
    return new ElasticsearchConfig(PREFIX, MAX_TASKS, REQUESTS_PER_SECOND, SITEMAP_SCROLL_SIZE, SITEMAP_COLLECTIONS_PER_SUBMAP, sitemapEnabled);
  }
}
