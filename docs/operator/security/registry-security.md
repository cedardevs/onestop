<div align="center"><a href="/onestop/operator">Operator Documentation Home</a></div>
<hr>

**Estimated Reading Time: 10 minutes**

# OneStop Registry Security
The OneStop REST API endpoints that use for publishing, updating and removing a record are secured via CAS authentication. 

The following endpoints that use HTTP Methods POST, PUT, PATCH, or DELETE:
- `/metadata/**`

The following endpoints that use HTTP Method GET:
- `/metadata/**/resurrection`

## Authentication

Examples of providing credentials:

```
# Example credentials
export CAS_USER=casuser
export CAS_PASSWORD=password

# Prompt CAS user 'casuser' for credentials via curl
curl -u "${CAS_USER}" \
-H "Content-Type: application/json" \
-X POST https://data.dev.ncei.noaa.gov/psi-registry/metadata/**

# Manually provide CAS user 'casuser' credentials via curl
curl -u "${CAS_USER}:${CAS_PASSWORD}" \
-H "Content-Type: application/json" \
-X POST https://data.dev.ncei.noaa.gov/psi-registry/metadata/**

# Manually provide CAS user 'casuser' credentials via curl and 'Authorization' header
curl \
-H "Authorization: Basic $(echo "${CAS_USER}:${CAS_PASSWORD}" | base64) " \
-H "Content-Type: application/json" \
-X POST https://data.dev.ncei.noaa.gov/psi-registry/metadata/**
```

However it is accomplished, it is required to provide the `Authorization` header with every secure request. The credentials can be hard-coded in the above commands, stored in environment variables, or otherwise retrieved and maintained from a properties file to prevent the need to prompt for credentials when working with batches of requests. In the case of a Java client, you could also retrieve credentials from a JKS (Java Keystore)

## Authorization

Authorization (privileged users) is a configuration on the OneStop Registry API. For CAS users who need the privileges to use the secured endpoints described above, they will need to be added to the `ROLE_ADMIN` role. In order to give this privilege to a new user, the security configuration will need to be updated:

### For example:
``` yml
spring:
  profiles: cas

cas:
  prefixUrl: 'https://auth.ncdc.noaa.gov/cas'

authorization:
  roles:
    ROLE_ADMIN:
      - ...
      - cas.user.who.needs.privilege
```

## Toggling Security Feature

Ensuring CAS security is enabled on the OneStop Registry API:

Under the hood, we use a spring profile 'cas' to enable security. The mechanisms for toggling are many, but we recommend one of the following (multiple profiles would be comma-delimited):

1. Via the YML config for OneStop: `spring.profiles.active:cas`
1. Via environment variable: `export SPRING_PROFILES_ACTIVE=cas`

## Service Accounts
The concept of "service accounts" is not yet being utilized for OneStop Registry on CAS, but one could theoretically be added for dedicated/automated service clients (non-humans) if the administrators of CAS are willing to maintain such a use case.

## Per-Request Authentication
OneStop Registry is not a UI or browser frontend, and is not intended to be exposed to the general public. The CAS instance it transacts with is using its own REST API that should not be exposed outside of an internal network to prevent brute-force dictionary attacks. This is why clients don't need to maintain an SSO session with OneStop Registry and require per-request credentials ("direct client").

## References: 
1. CAS Rest Protocol Documenation
  - https://apereo.github.io/cas/6.0.x/protocol/REST-Protocol.html#rest-protocol
1. Pac4j Guidance on Securing REST APIs
  - https://www.pac4j.org/blog/spring-webmvc-pac4j-vs-spring-security-round-2-rest-apis.html  

<hr>
<div align="center"><a href="#">Top of Page</a></div>
