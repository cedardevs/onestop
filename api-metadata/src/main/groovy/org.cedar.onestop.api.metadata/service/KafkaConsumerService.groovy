package org.cedar.onestop.api.metadata.service

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.cedar.schemas.avro.psi.ParsedRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Slf4j
@Service
@Profile("kafka-ingest")
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
      List<Map> valuesIds = records.collect {
        String id = it.key()
        ParsedRecord record = it.value()
        InventoryManagerToOneStopUtil.validateMessage(id, record) ?
            [id: id, parsedRecord: record] as Map :
            null
        
      }
      valuesIds.removeAll(Collections.singleton(null))
      metadataManagementService.loadParsedMetadata(valuesIds)
      
    } catch (Exception e) {
      log.error("Unexpected error", e)
    }
    
  }

}
