package org.cedar.onestop.api.admin.authorization.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

@RestController
class TestController {

    @GetMapping(path="/test/permitAll")
    String helloWorld() {
        return 'This endpoint should be accessible to all.'
    }

    @GetMapping(path="/test/userOnly")
    String helloUser() {
        return 'This endpoint is accessible to users with USER role.'
    }
}
