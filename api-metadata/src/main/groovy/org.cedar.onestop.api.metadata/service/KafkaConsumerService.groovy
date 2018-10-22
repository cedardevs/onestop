package org.cedar.onestop.api.metadata.service

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service


@Slf4j
@Service
@ConditionalOnProperty("features.kafka.consumer")
class KafkaConsumerService {
  @Value('${kafka.topic.PARSED_COLLECTIONS_TOPIC}')
  String parsedCollectionTopic
  
  @Value('${kafka.topic.PARSED_GRANULES_TOPIC}')
  String parsedGranulesTopic
  
  @Autowired
  private MetadataManagementService metadataManagementService
  
  @KafkaListener(topics = ['${kafka.topic.PARSED_COLLECTIONS_TOPIC}', '${kafka.topic.PARSED_GRANULES_TOPIC}'])
  void listen(List<ConsumerRecord<String, String>> records) {
    // Update collections & granules
    def slurper = new JsonSlurper()
    log.info("consuming message from kafka topic")
    try {
      List<Map> valuesIds = records.collect {
        def id = it.key()
        def messageMap = slurper.parseText(it.value() as String) as Map
        InventoryManagerToOneStopUtil.validateMessage(messageMap, id) ?
            [id: id, discovery: messageMap.discovery, analysis: messageMap.analysis] as Map :
            null
        
      }
      valuesIds.removeAll(Collections.singleton(null))
      metadataManagementService.loadParsedMetadata(valuesIds)
      
    } catch (Exception e) {
      log.error("Unexpected error", e)
    }
    
  }
}
