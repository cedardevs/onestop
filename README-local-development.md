# Developer Guide
## Quick Start
### System Requirements
- Java >= 8 (needed by Gradle 5 wrapper)
- Docker (needed to run test containers in integration tests)
- Elasticsearch 5.6.14 (running on port 9200)
### Clone
`git clone https://github.com/cedardevs/onestop.git`
### Build
<details>
  <summary>
    <code>./gradlew build</code>
  </summary>
  <br/>
  <p>For individual components:</p>
<pre>./gradlew build:api-metadata
./gradlew build:api-search
./gradlew build:client</pre>
</details>
### 


`./gradlew uploadTestData`


<details>
<summary>Something is really messed up in my environment, what should I do?</summary>
<p>
If you find yourself in a weird state, even after freshly cloning the project, you might try some or all of the following techniques to reset your environment:

```

```
</p>
</details>

# Quick Start (Kubernetes + Helm + Skaffold)
```
# System Requirements
# kubernetes, helm, skaffold

```

# Local Development using Skaffold

To run the k8s stack locally, use `skaffold dev -f skaffold.yaml`.

This requires setting up the keystore as a secret. Due to the sensitive nature of certs, this is a one-time setup which is done separately. See [private instructions](https://github.com/cedardevs/help/wiki/local-secure-development-setup) for how to do this setup.

# Enabling security features

By default, security is disabled locally. To turn it on, change onestop-api-metadata.yaml in the k8s deployments from
```
- name: SPRING_PROFILES_ACTIVE
  value: "securitydisabled"
```
to
```
- name: SPRING_PROFILES_ACTIVE
  value: "securityenabled"
```

There is currently no configmap in the metadata deployment to make changes to the security configuration (such as adding yourself as an admin). If you need to add one, see the api-search config for how to add a configmap. The configuration needed is:
```
---
spring:
  profiles: securityenabled
  user.roles: yourOfficalEmail:ADMIN
```
This structure essentially provides a new application-securityenabled.yml to the application.
