package org.cedar.onestop.kafka.common.conf;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KafkaConfigNames {

  public static final Set<String> avro = Set.of(
      REQUEST_HEADER_PREFIX,
      SCHEMA_REGISTRY_URL_CONFIG,
      MAX_SCHEMAS_PER_SUBJECT_CONFIG,
      AUTO_REGISTER_SCHEMAS,
      BASIC_AUTH_CREDENTIALS_SOURCE,
      BEARER_AUTH_CREDENTIALS_SOURCE,
      USER_INFO_CONFIG,
      BEARER_AUTH_TOKEN_CONFIG,
      KEY_SUBJECT_NAME_STRATEGY,
      VALUE_SUBJECT_NAME_STRATEGY
  );
  public static final Set<String> admin = AdminClientConfig.configNames();
  public static final Set<String> producer = combineSets(ProducerConfig.configNames(), avro);
  public static final Set<String> consumer = combineSets(ConsumerConfig.configNames(), avro);
  public static final Set<String> streams = combineSets(producer, consumer);

  @SafeVarargs
  private static final <T> Set<T> combineSets(Set<T>... sets) {
    return Stream.of(sets).flatMap(Set::stream).collect(Collectors.toSet());
  }

}
