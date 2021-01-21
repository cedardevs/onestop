package org.cedar.onestop.gateway.config;

public class LoginGovConstants {
  public static final String LOGIN_GOV_REGISTRATION_ID = "login-gov";
  public static final String LOGIN_GOV_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
  public static final Long LOGIN_GOV_TOKEN_EXPIRATION_TIME = 3600000L; // 1 hour
  public static final String LOGIN_SUCCESS_ENDPOINT = "/";
  public static final String LOGIN_FAILURE_ENDPOINT = "/";
  public static final String LOGOUT_ENDPOINT = "/logout";


  // AUTHORIZATION
  // https://developers.login.gov/oidc/#authorization

  // `acr_values`
  // The Authentication Context Class Reference values used to specify the IAL (Identity Assurance Level) of an account,
  // either IAL1, or IAL2. This and the scope determine which user attributes will be available in the user info response.
  // The possible parameter values are:
  public static final String LOGIN_GOV_IAL1 = "http://idmanagement.gov/ns/assurance/ial/1";
  public static final String LOGIN_GOV_IAL2 = "http://idmanagement.gov/ns/assurance/ial/2";

  // Logout Constants
  public static final String LOGIN_GOV_LOGOUT_ENDPOINT = "https://idp.int.identitysandbox.gov/openid_connect/logout";
  public static final String LOGIN_GOV_LOGOUT_PARAM_ID_TOKEN_HINT = "id_token_hint";
  public static final String LOGIN_GOV_LOGOUT_PARAM_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
  public static final String LOGIN_GOV_LOGOUT_PARAM_STATE = "state";
}
