package org.cedar.onestop.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

  @GetMapping("/hello")
  public Map<String, String> hello(final @AuthenticationPrincipal Jwt jwt) {
    if(jwt != null) {
      System.out.println("headers:\n" + jwt.getHeaders());
      System.out.println("\nclaims:\n" + jwt.getClaims());
      return Collections.singletonMap("message", "Hello " + jwt.getClaimAsString("name"));
    }
    else {
      System.out.println("JWT is null");
      return Collections.singletonMap("message", "no jwt");
    }
  }

}
