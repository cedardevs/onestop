package org.cedar.onestop.api.metadata.authorization.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

@RestController
class TestController {

    @GetMapping(path="/test")
    String helloWorld() {
        return 'Hello World'
    }
}
