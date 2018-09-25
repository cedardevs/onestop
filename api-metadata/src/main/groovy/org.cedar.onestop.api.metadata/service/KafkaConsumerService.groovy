package org.cedar.onestop.api.metadata.service

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.stereotype.Service

import java.util.concurrent.CountDownLatch

@Slf4j
@Service
class KafkaConsumerService {
  @Value('${kafka.topic.PARSED_COLLECTIONS_TOPIC}')
  String parsedCollectionTopic
  
  @Value('${kafka.topic.PARSED_GRANULES_TOPIC}')
  String parsedGranulesTopic
  
  @Autowired
  private MetadataManagementService metadataManagementService
  // @KafkaListener(topics = ['parsedCollectionTopic', 'parsedGranulesTopic'])
  @KafkaListener(topics = ['${kafka.topic.PARSED_COLLECTIONS_TOPIC}', '${kafka.topic.PARSED_GRANULES_TOPIC}'])
  void listen(List<ConsumerRecord<String, String>> records) {
    // Update collections & granules
    def slurper = new JsonSlurper()
    log.info("consuming message from a topic ${records.topic}")
    try {
      def valuesIds = records.collect {
        def id = it.key()
        def messageMap = slurper.parseText(it.value() as String) as Map
        validateMessages(messageMap) ?
            [id: id , discovery: messageMap.discovery ] :
              null
        
      }
      valuesIds.removeAll(Collections.singleton(null))
      metadataManagementService.loadParsedMetadata(valuesIds as Map)
      
    } catch (Exception e) {
      log.error("Unexpected error", e)
    }
  
  }
  
  Boolean validateMessages(Map messageMap) {
      def analysis = messageMap.analysis
      def isValid = false
      def title = analysis.titles['title'] as Map
      def fileIdentifier = analysis.identification['fileIdentifier'] as Map
      if (!title.exists || !fileIdentifier.exists) {
        return isValid
      } else {
        isValid = true
        return isValid
      }
  }
}
