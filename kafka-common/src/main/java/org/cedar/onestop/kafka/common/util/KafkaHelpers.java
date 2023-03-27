package org.cedar.onestop.kafka.common.util;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.cedar.onestop.data.util.MapUtils;
import org.cedar.onestop.kafka.common.conf.AppConfig;
import org.cedar.onestop.kafka.common.conf.KafkaConfigNames;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.LogAndContinueExceptionHandler;
import org.cedar.onestop.kafka.common.util.IgnoreRecordTooLargeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.apache.kafka.streams.KafkaStreams.State.ERROR;
import static org.apache.kafka.streams.KafkaStreams.State.NOT_RUNNING;
import static org.apache.kafka.streams.StreamsConfig.*;

public class KafkaHelpers {
  private static final Logger log = LoggerFactory.getLogger(KafkaHelpers.class);

  public static CompletableFuture<Object> onError(KafkaStreams streams, StreamsUncaughtExceptionHandler handler) {
    var stateFuture = new CompletableFuture<KafkaStreams.State>();
    streams.setStateListener((newState, oldState) -> {
      if (!stateFuture.isDone() && (newState == ERROR || newState == NOT_RUNNING)) {
        log.error("Streams app entered a bad state: " + newState + "");
        stateFuture.complete(newState);
      }
    });

    streams.setUncaughtExceptionHandler(handler);

    return CompletableFuture.anyOf(stateFuture);
  }

  public static CreateTopicsResult ensureTopics(AdminClient client, Collection<String> names, int partitions, short replicas) throws ExecutionException, InterruptedException {
    return ensureTopics(client, names, partitions, replicas, null);
  }

  public static CreateTopicsResult ensureTopics(AdminClient client, Collection<String> names, int partitions, short replicas, Map<String, String> config) throws ExecutionException, InterruptedException {
    var definitions = names.stream().map(name -> new TopicDefinition(name, partitions, replicas, config)).collect(Collectors.toSet());
    return ensureTopics(client, definitions);
  }

  public static CreateTopicsResult ensureTopic(AdminClient client, TopicDefinition topicDefinitions) throws ExecutionException, InterruptedException {
    return ensureTopics(client, List.of(topicDefinitions));
  }

  public static CreateTopicsResult ensureTopics(AdminClient client, Collection<TopicDefinition> topicDefinitions) throws ExecutionException, InterruptedException {
    var existingTopics = client.listTopics().names().get();
    var newTopics = topicDefinitions.stream()
        .filter(def -> !existingTopics.contains(def.name))
        .map(TopicDefinition::toNewTopicCommand)
        .collect(Collectors.toSet());
    return client.createTopics(newTopics, new CreateTopicsOptions().timeoutMs(30000));
  }

  public static Properties buildAdminConfig(AppConfig config) {
    // Filter to only valid config values -- Admin config + possible internal Producer & Consumer config
    var kafkaConfigs = MapUtils.trimMapKeys("kafka.", config.getCurrentConfigMap());
    var filteredConfigs = MapUtils.filterMapKeys(KafkaConfigNames.admin, kafkaConfigs);

    log.info("Building admin client config for {}", StreamsApps.INDEXER_ID);
    Properties streamsConfiguration = new Properties();
    streamsConfiguration.putAll(filteredConfigs);
    return streamsConfiguration;
  }

  public static Properties buildStreamsConfig(AppConfig config) {
    // Filter to only valid config values -- Streams config + possible internal Producer & Consumer config
    var kafkaConfigs = MapUtils.trimMapKeys("kafka.", config.getCurrentConfigMap());
    var filteredConfigs = MapUtils.filterMapKeys(KafkaConfigNames.streams, kafkaConfigs);

    log.info("Building kafka streams appConfig for {}", StreamsApps.INDEXER_ID);
    Properties streamsConfiguration = new Properties();
    streamsConfiguration.put(APPLICATION_ID_CONFIG, StreamsApps.INDEXER_ID);
    streamsConfiguration.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    streamsConfiguration.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class.getName());
    streamsConfiguration.put(DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName());
    streamsConfiguration.put(DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, IgnoreRecordTooLargeHandler.class.getName());
    streamsConfiguration.putAll(filteredConfigs);
    return streamsConfiguration;
  }

  public static Map<String, Object> buildAvroSerdeConfig(AppConfig config) {
    // Filter to only valid config values -- avro/schema registry values
    var kafkaConfigs = MapUtils.trimMapKeys("kafka.", config.getCurrentConfigMap());
    return MapUtils.filterMapKeys(KafkaConfigNames.avro, kafkaConfigs);
  }

  public static class TopicDefinition {
    public final String name;
    public final int partitions;
    public final short replicas;
    public final Map<String, String> config;

    public TopicDefinition(String name, int partitions, short replicas) {
      this(name, partitions, replicas, null);
    }

    public TopicDefinition(String name, int partitions, short replicas, Map<String, String> config) {
      this.name = name;
      this.partitions = partitions;
      this.replicas = replicas;
      this.config = config;
    }

    public NewTopic toNewTopicCommand() {
      var result = new NewTopic(name, partitions, replicas);
      if (config != null && !config.isEmpty()) {
        result.configs(config);
      }
      return result;
    }
  }

}
