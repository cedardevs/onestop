package org.cedar.onestop.gateway.config;

class LoginGovConstants {
  static final String LOGIN_GOV_REGISTRATION_ID = "login-gov";
  static final String LOGIN_GOV_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
  static final Long LOGIN_GOV_TOKEN_EXPIRATION_TIME = 3600000L; // 1 hour
  static final String LOGIN_SUCCESS_ENDPOINT = "/";
  static final String LOGIN_FAILURE_ENDPOINT = "/";
  static final String LOGOUT_ENDPOINT = "/logout";


  // AUTHORIZATION
  // https://developers.login.gov/oidc/#authorization

  // `acr_values`
  // The Authentication Context Class Reference values used to specify the IAL (Identity Assurance Level) of an account,
  // either IAL1, or IAL2. This and the scope determine which user attributes will be available in the user info response.
  // The possible parameter values are:
  static final String LOGIN_GOV_IAL1 = "http://idmanagement.gov/ns/assurance/ial/1";
  static final String LOGIN_GOV_IAL2 = "http://idmanagement.gov/ns/assurance/ial/2";

  // Logout Constants
  static final String LOGIN_GOV_LOGOUT_ENDPOINT = "https://idp.int.identitysandbox.gov/openid_connect/logout";
  static final String LOGIN_GOV_LOGOUT_PARAM_ID_TOKEN_HINT = "id_token_hint";
  static final String LOGIN_GOV_LOGOUT_PARAM_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
  static final String LOGIN_GOV_LOGOUT_PARAM_STATE = "state";
}
