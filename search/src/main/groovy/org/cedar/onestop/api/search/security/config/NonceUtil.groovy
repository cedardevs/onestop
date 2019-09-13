package org.cedar.onestop.api.search.security.config

import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser

/**
 * Stores nonces in memory
 */
class NonceUtil {
  public static Set nonces = []

  static String extractAndCheckNonce(OAuth2AuthenticationToken authentication){
    OidcUser user = authentication.principal as OidcUser
    String idToken = user.idToken.tokenValue
    //extract token, parse, assert we issued it and remove it
    nonces.remove((new JsonSlurper().parseText(new String(new Base64(true).decode(idToken.split("\\.")[1]))))?.nonce)
  }

  static String generateNonce(String sessionId){
    String nonce = RandomStringUtils.random(32, sessionId.toCharArray())
    nonces.add(nonce)
    return nonce
  }
}
