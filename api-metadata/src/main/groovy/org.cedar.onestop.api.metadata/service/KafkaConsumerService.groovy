package org.cedar.onestop.api.metadata.service

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

import java.time.temporal.ChronoUnit


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
    log.info("consuming message from a topic ${records.topic}")
    try {
      def valuesIds = records.collect {
        def id = it.key()
        def messageMap = slurper.parseText(it.value() as String) as Map
        validateMessages(messageMap) ?
            [id: id, discovery: messageMap.discovery, analysis: messageMap.analysis] :
            null
        
      }
      valuesIds.removeAll(Collections.singleton(null))
      metadataManagementService.loadParsedMetadata(valuesIds as Map)
      
    } catch (Exception e) {
      log.error("Unexpected error", e)
    }
    
  }
  
  Boolean validateMessages(Map messageMap) {
    // FIXME add id to log messages to make them parseable
    def analysis = messageMap.analysis
    def isValid = false
    def title = analysis.titles['title'] as Map
    def fileIdentifier = analysis.identification['fileIdentifier'] as Map
    def hierarchyLevel = analysis.identification['hierarchyLevelName'] as Map
    def beginDate = analysis.temporalBounding['begin'] as Map
    def endDate = analysis.temporalBounding['end'] as Map
    //validate record
    if (!title.exists || !fileIdentifier.exists) {
      log.info("Missing title or fileIdentifier detected")
      return isValid
    } else if (!hierarchyLevel.matchesIdentifiers && fileIdentifier.fileIdentifierString == null) {
      log.info("Mismatch between metadata type and corresponding identifiers detected")
      return isValid
    } else if (beginDate.utcDateTimeString == 'INVALID' || endDate.utcDateTimeString == 'INVALID') {
      log.info("Invalid begin and/or end date")
      return isValid
    } else {
      isValid = true
      return isValid
    }
  }
}
