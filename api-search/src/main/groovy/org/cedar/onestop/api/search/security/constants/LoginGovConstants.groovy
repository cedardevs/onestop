package org.cedar.onestop.api.search.security.constants

class LoginGovConstants {
    static final String LOGIN_GOV_REGISTRATION_ID = "logingov"
    static final String LOGIN_GOV_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
    static final Long LOGIN_GOV_TOKEN_EXPIRATION_TIME = 864000000 // 10 days

    // The Authentication Context Class Reference values used to specify the LOA (level of assurance)
    // of an account, either LOA1 or LOA3. This and the scope determine which user attributes will be available
    // in the user info response. The possible parameter values are:
    static final String LOGIN_GOV_LOA1 = "http://idmanagement.gov/ns/assurance/loa/1"
    static final String LOGIN_GOV_LOA3 = "http://idmanagement.gov/ns/assurance/loa/3"
}