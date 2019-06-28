package org.cedar.psi.registry.security

import org.springframework.context.annotation.Profile
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Profile('cas')
@RestController
class CASController {

//  @GetMapping("/login")
//  String login() {
//    return "redirect:/secured"
//  }

  @GetMapping("/logout")
  String logout(HttpServletRequest request, HttpServletResponse response, SecurityContextLogoutHandler logoutHandler) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication()
    logoutHandler.logout(request, response, auth)
    new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY).logout(request, response, auth)
    return [ response: "You have logged out of CAS-secured PSI Registry service successfully." ] as Map
  }

}
