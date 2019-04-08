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
