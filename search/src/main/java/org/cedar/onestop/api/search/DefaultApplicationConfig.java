package org.cedar.onestop.api.search;

import org.cedar.onestop.elastic.common.ElasticsearchClient;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.elastic.common.ElasticsearchVersion;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
  List<String> elasticHosts;

  @Value("${elasticsearch.ssl.enabled:false}")
  Boolean sslEnabled;

  @Value("${elasticsearch.ssl.cert.path:}")
  String certPath;

  @Value("${elasticsearch.ssl.cert.tls:}")
  String certTLS;

  @Value("${elasticsearch.ro.user:}")
  String roUser;

  @Value("${elasticsearch.ro.pass:}")
  String roPass;

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

  @Bean(name = "elasticsearchVersion")
  @DependsOn("restHighLevelClient")
  ElasticsearchVersion elasticsearchVersion(RestHighLevelClient restHighLevelClient) throws IOException {
    // check for compatible elastic version (will throw exception if not compatible)
    return new ElasticsearchVersion(restHighLevelClient);
  }

  // we dont' want this bean to be created when the tests use test containers,
  // but in our CI environment, we have a separate Elasticsearch and so we can leverage this normal RestHighLevelClient again
  @Profile({"!integration", "ci"})
  @Bean(name = "restHighLevelClient", destroyMethod = "close")
  RestHighLevelClient restHighLevelClient() {
    String certFilePath = certPath + "/" + certTLS;
    return ElasticsearchClient.create(elasticHosts, elasticPort, sslEnabled, certFilePath, roUser, roPass);
  }

  @Bean
  @DependsOn("elasticsearchVersion")
  ElasticsearchConfig elasticsearchConfig(ElasticsearchVersion elasticsearchVersion) throws IOException {
    Boolean sitemapEnabled = Arrays.stream(environment.getActiveProfiles()).anyMatch(profile -> profile.compareTo("sitemap") == 0);
    return new ElasticsearchConfig(elasticsearchVersion, PREFIX, MAX_TASKS, REQUESTS_PER_SECOND, SITEMAP_SCROLL_SIZE, SITEMAP_COLLECTIONS_PER_SUBMAP, sitemapEnabled);
  }
}
