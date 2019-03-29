# Developer Guide
## Quick Start
### Basic System Requirements
- Java >= 8
  - needed by Gradle 5 wrapper
- Docker
  - needed to run test containers in integration tests
- Elasticsearch 5.6.14
  - running on port 9200
- Node
  - needed to hot-reload client via npm/webpack-dev-server  
### Clone
`git clone https://github.com/cedardevs/onestop.git`
### Build
<details open>
  <summary>
    <code>./gradlew build</code>
  </summary>
  <br/>
  <p>For individual components:</p>
<pre>./gradlew api-metadata:build
./gradlew api-search:build
./gradlew client:build</pre>
</details>

### Run
```
./gradlew api-metadata:bootrun
./gradlew api-search:bootrun
cd client && npm run dev
```
### Upload Test Data
```
./gradlew uploadTestData
```

# Quick Start (Kubernetes + Helm + Skaffold)
### System Requirements
- "Basic System Requirements" listed above
  - *excluding* local Elasticsearch installation
- Kubernetes
  - our team enables Kubernetes with Docker Desktop (see: `Preferences...` > `Kubernetes`)
  - we highly recommend allocating >= 6.0 GiB to Docker (see: `Preferences...` > `Advanced`)
- Helm
- Skaffold

```
brew install kubernetes-helm
brew install skaffold

# install tiller onto the cluster (one-time deal, unless upgrading helm)
helm init

# run
skaffold dev -f skaffold.yaml
```

##### If running client via Node
```
# 1) comment out client sections in skaffold.yaml

# 2) tell webpack-dev-server proxy to use the k8s exposed port for the search API
cd client && npm run kub

# 3) run skaffold without automatic port forwarding
skaffold dev --port-forward=false -f skaffold.yaml
```

### Troubleshooting
Something is really messed up in my environment, what should I do?

If you find yourself in a weird state, even after freshly cloning the project, you might try some or all of the following techniques to reset your environment before building and running:

##### Clear Caches
```
./gradle clean
rm -rf ~/.gradle/caches
cd client && rm -rf node_modules/
```

##### Resetting Kubernetes Tools
```
# update chart repositories
helm repo update

# rebuild the charts/ directory based on the requirements.lock file
helm dependency build

# list the dependencies for the given chart
helm dependency list

# update charts/ based on the contents of requirements.yaml
helm dependency update

helm delete elasticsearch --purge
helm delete api-admin --purge
helm delete api-search --purge
helm delete client --purge

skaffold delete
```

# Enabling security features

Turning on security-related features requires setting up the keystore as a secret. Due to the sensitive nature of certs, this is a one-time setup which is done separately. See [private instructions](https://github.com/cedardevs/help/wiki/local-secure-development-setup) for how to do this setup.

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
