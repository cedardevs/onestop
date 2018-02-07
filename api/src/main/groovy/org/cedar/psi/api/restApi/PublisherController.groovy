package org.cedar.psi.api.restApi

import org.cedar.psi.api.services.InvPublisher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value="/metadata")
class PublisherController {

    @Autowired
    InvPublisher producer

    private static final Logger log = LoggerFactory.getLogger(InvPublisher.class)

    @RequestMapping(value="/granule", consumes="application/json")
    void producer(@RequestBody String data)
            throws Exception {
        !data.contains("trackingId") ? log.info("payload missing tracking id") : producer.publishGranule(data)
    }
}