package org.cedar.onestop.api.admin.service

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.velocity.runtime.directive.Parse
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.parse.DefaultParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

import java.util.function.Function
import java.util.stream.Collectors

@Slf4j
@Service
@Profile(["kafka-ingest"])
class KafkaConsumerService {

  @Value('${kafka.topic.collections}')
  String parsedCollectionTopic

  @Value('${kafka.topic.granules}')
  String parsedGranulesTopic

  @Autowired
  private MetadataManagementService metadataManagementService

  @KafkaListener(topics = ['${kafka.topic.collections}', '${kafka.topic.granules}'])
  void listen(List<ConsumerRecord<String, ParsedRecord>> records) {
    // Update collections & granules
    log.info("consuming message from kafka topic")
    try {
      // fills in missing fields in parsed records and classifies them as "tombstone" "valid" or "invalid"
      def groupedRecords = records.stream()
          .filter({ it != null })
          .map(this.&consumerRecordToMap)
          .collect(Collectors.groupingBy(this.&classifyResults))

      def tombstones = groupedRecords.getOrDefault("tombstone", Collections.emptyList())
      tombstones.forEach({
        log.info("Handling tombstone message for record [${it.id}]")
        metadataManagementService.deleteMetadata(it.id as String, true, false)
      })
      def validRecords = groupedRecords.getOrDefault("valid", Collections.emptyList())
      if (validRecords.size() > 0) {
        validRecords.forEach({ log.info("Indexing valid record [${it.id}]") })
        metadataManagementService.loadParsedRecords(validRecords)
      }
      def invalidRecords = groupedRecords.getOrDefault("invalid", Collections.emptyList())
      invalidRecords.forEach({
        log.info("Ignoring record [${it.id}] as it is not valid for onestop indexing")
      })
    }
    catch (Exception e) {
      log.error("Unexpected error", e)
    }
  }

  /**
   * Fills in default discovery and analysis information on a record if it does not already have them
   * @param record The record
   * @return The filled-in record, or null if the input record itself is null
   */
  private static ParsedRecord fillInRecord(ParsedRecord record) {
    if (record != null && record.discovery == null) {
      record = DefaultParser.addDiscoveryToParsedRecord(record)
    }
    if (record != null && record.analysis == null) {
      record = Analyzers.addAnalysis(record)
    }
    return record
  }

  /**
   * Transforms a Kafka ConsumerRecord to a Map with keys "id" and "parsedRecord"
   * Fills in the parsedRecord with {@link #fillInRecord}
   * @param record The ConsumerRecord
   * @return The Map
   */
  private static Map<String, Object> consumerRecordToMap(ConsumerRecord<String, ParsedRecord> record) {
    return [id: record.key(), parsedRecord: fillInRecord(record.value())]
  }

  /**
   * Classifies a map produced by {@link #consumerRecordToMap} as "tombstone", "valid", or "invalid"
   * @param result The Map
   * @return The classifier string
   */
  private static String classifyResults(Map result) {
    return result.parsedRecord == null ? "tombstone" :
        Indexer.validateMessage(result.id, result.parsedRecord)?.valid ? "valid" :
            "invalid"
  }

}
