package org.cedar.onestop.api.metadata.service

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Slf4j
@Service
class KafkaConsumerService {
  @Value('${kafka.topic.PARSED_COLLECTIONS_TOPIC}')
  private String parsedCollectionTopic
  
  @Autowired
  private MetadataManagementService metadataManagementService
  
  @KafkaListener(topics = '${kafka.topic.PARSED_COLLECTIONS_TOPIC}')
  Map listen(ConsumerRecord record) {
    def message = record.value() as String
    def id = record.key() as String
  
    try {
      log.info("consuming message from a topic ...")
      def slurper = new JsonSlurper()
      def messageMap = slurper.parseText(message) as Map
  
      def analysis = messageMap.analysis as Map
      def title = analysis.titles['title'] as Map
      def fileIdentifier = analysis.identification['fileIdentifier'] as Map
      def messageToMap = [id: id, discovery: messageMap.discovery]
      if (!title.exists || !fileIdentifier.exists) {
        log.error("message is not valid: title: ${title.exists} and fileIdentifier: ${fileIdentifier.exists}")
      } else {
        return metadataManagementService.loadParsedMetadata(messageToMap)
      }
    } catch (Exception e) {
      log.error("Unexpected error", e)
    }
  }
}
