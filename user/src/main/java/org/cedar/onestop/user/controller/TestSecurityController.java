package org.cedar.onestop.user.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestSecurityController {

  @GetMapping(value = "/open", produces = MediaType.APPLICATION_JSON_VALUE)
  public String open() {
    return "{ \"data\": \"open\" }";
  }

  @GetMapping(value = "/closed", produces = MediaType.APPLICATION_JSON_VALUE)
  public String closed() {
    return "{ \"data\": \"closed\" }";
  }

}
