package org.cedar.onestop.api.admin.service

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.parse.DefaultParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

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
      def partitionedRecords = records.stream()
          .filter({ it != null })
          .map({ [id: it.key(), parsedRecord: fillInRecord(it.value())] })
          .filter({it.parsedRecord == null || Indexer.validateMessage(it.id, it.parsedRecord)?.valid})
          .collect(Collectors.partitioningBy({it.parsedRecord == null}))

      def tombstones = partitionedRecords.getOrDefault(true, Collections.emptyList())
      tombstones.forEach({
        metadataManagementService.deleteMetadata(it.id as String, true, false)
      })
      def validRecords = partitionedRecords.getOrDefault(false, Collections.emptyList())
      if (validRecords.size() > 0) {
        metadataManagementService.loadParsedRecords(validRecords)
      }
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

}
