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
    log.info("consuming message from a topic ${records.topic}")
    try {
      def valuesIds = records.collect {
        def id = it.key()
        def messageMap = slurper.parseText(it.value() as String) as Map
        validateMessages(messageMap, id) ?
            [id: id, discovery: messageMap.discovery, analysis: messageMap.analysis] :
            null
        
      }
      valuesIds.removeAll(Collections.singleton(null))
      metadataManagementService.loadParsedMetadata(valuesIds as Map)
      
    } catch (Exception e) {
      log.error("Unexpected error", e)
    }
    
  }
  
  Boolean validateMessages(Map messageMap, String id) {
    def analysis = messageMap.analysis
    def title = analysis.titles['title'] as Map
    def fileIdentifier = analysis.identification['fileIdentifier'] as Map
    def parentIdentifier = analysis.identification['parentIdentifier'] as Map
    def beginDate = analysis.temporalBounding['begin'] as Map
    def endDate = analysis.temporalBounding['end'] as Map

    String failureMsg = "INVALID RECORD [ $id ]. VALIDATION FAILURES: "
    def failures = []

    // Validate record
    if(!fileIdentifier.exists) {
      failures.add('Missing fileIdentifier')
    }
    if(!title.exists) {
      failures.add('Missing title')
    }
    if(messageMap.discovery.hierarchyLevelName == 'granule' && !parentIdentifier.exists) {
      failures.add('Mismatch between metadata type and identifiers detected')
    }
    if(beginDate.utcDateTimeString == 'INVALID') {
      failures.add('Invalid beginDate')
    }
    if(endDate.utcDateTimeString == 'INVALID') {
      failures.add('Invalid endDate')
    }

    if(!failures) {
      return true
    }
    else {
      failureMsg += "[ ${failures.join(', ')} ]"
      log.info(failureMsg)
      return false
    }


//    } else if (!hierarchyLevel.matchesIdentifiers && fileIdentifier.fileIdentifierString == null) {
//      log.info("Mismatch between metadata type and corresponding identifiers detected.")
//      return isValid
//    } else if (beginDate.utcDateTimeString == 'INVALID' || endDate.utcDateTimeString == 'INVALID') {
//      log.info("Invalid begin and/or end date.")
//      return isValid
//    } else {
//      isValid = true
//      return isValid
//    }
  }
}
