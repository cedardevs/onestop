package org.cedar.onestop.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
  private static final Logger log = LoggerFactory.getLogger(AdminController.class);

  @GetMapping("/hello")
  public Map<String, String> hello(final @AuthenticationPrincipal Authentication authentication) {
    Map response;
    if(authentication != null) {
      log.debug("details:\n" + authentication.getDetails().toString());
      log.debug("\nauthorities:\n" + authentication.getAuthorities().toString());
      response = Collections.singletonMap("message", "Hello " + authentication.getName());
    }
    else {
      log.debug("authentication is null");
      response = Collections.singletonMap("message", "no authentication");
    }
    return response;
  }

}
