package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.apache.kafka.streams.KafkaStreams.State.ERROR;
import static org.apache.kafka.streams.KafkaStreams.State.NOT_RUNNING;

public class KafkaHelpers {
  private static final Logger log = LoggerFactory.getLogger(KafkaHelpers.class);

  public static KafkaStreams buildStreamsAppWithKillSwitch(Topology topology, Properties streamsConfig) {
    var killSwitch = new CompletableFuture<KafkaStreams.State>();
    killSwitch.thenAcceptAsync((state) -> {
      log.error("kill switch triggered with state [" + state + "] exiting...");
      System.exit(1);
    });
    KafkaStreams.StateListener killSwitchListener = (newState, oldState) -> {
      if (!killSwitch.isDone() && (newState == ERROR || newState == NOT_RUNNING)) {
        log.error("app entered bad state " + newState + ", executing kill switch");
        killSwitch.complete(newState);
      }
    };

    var app = new KafkaStreams(topology, streamsConfig);
    app.setStateListener(killSwitchListener);
    return app;
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
