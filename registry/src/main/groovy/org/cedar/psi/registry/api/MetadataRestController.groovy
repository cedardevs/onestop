package org.cedar.psi.registry.api

import groovy.util.logging.Slf4j
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.cedar.psi.registry.stream.RawGranuleStreamConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.PUT
import static org.springframework.web.bind.annotation.RequestMethod.HEAD

@Slf4j
@RestController
class MetadataRestController {

  private KafkaStreams rawGranuleStream

  @Autowired
  MetadataRestController(KafkaStreams rawGranuleStream) {
    this.rawGranuleStream = rawGranuleStream
  }


  @RequestMapping(path = '/metadata/{type}/{id}', method = [GET, HEAD], produces = 'application/json')
  Map retrieveJson(@PathVariable String type, @PathVariable String id, HttpServletResponse response) {
    if (type == 'granule') {
      def granuleStore = this.rawGranuleStream.store(RawGranuleStreamConfig.storeName, QueryableStoreTypes.keyValueStore())
      return [id: id, value: granuleStore.get(id)]
    }
    response.sendError(404)
    return null
  }

//  @RequestMapping(path='/metadata/rebuild', method = [PUT], produces = 'application/json')
//  Map rebuild() {
//    this.rawGranuleStream.close()
//    this.rawGranuleStream.cleanUp()
//    this.rawGranuleStream.start()
//    return ['acknowledged': true]
//  }

}
