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
@Profile(["kafka-ingest", "migration-ingest"])
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
      def validRecords = records.stream()
          .filter({ (it != null)} )
          .map({
            if(it.value().discovery == null && it.value().analysis == null) {
              [id: it.key(), parsedRecord: Analyzers.addAnalysis(DefaultParser.addDiscoveryToParsedRecord(it.value()))]
            }
            else {
              [id: it.key(), parsedRecord: it.value()]
            }
           })
          .filter({Indexer.validateMessage(it.id, it.parsedRecord)?.valid})
          .collect(Collectors.toList())

      if (validRecords.size() > 0) {
        metadataManagementService.loadParsedRecords(validRecords)
      }
    }
    catch (Exception e) {
      log.error("Unexpected error", e)
    }
  }

}
