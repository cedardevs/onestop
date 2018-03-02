package org.cedar.psi.api.restApi

import org.cedar.psi.api.services.InvPublisher

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value="/metadata")
class PublisherController {

    @Autowired
    InvPublisher producer

    @RequestMapping(value="/granule", consumes="application/json")
    void producer(@RequestBody String data) throws Exception {
        producer.publishGranule(data)
    }
}
