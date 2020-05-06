package org.cedar.onestop.user.controller;

import org.springframework.security.core.Authentication;
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
  public Map<String, String> hello(final @AuthenticationPrincipal Authentication authentication) {
    if(authentication != null) {
      System.out.println("details:\n" + authentication.getDetails().toString());
      System.out.println("\nauthorities:\n" + authentication.getAuthorities().toString());
      return Collections.singletonMap("message", "Hello " + authentication.getName());
    }
    else {
      System.out.println("authentication is null");
      return Collections.singletonMap("message", "no authentication");
    }
  }

}
