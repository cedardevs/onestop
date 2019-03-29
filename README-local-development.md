# Developer Guide
## Quick Start
### Basic System Requirements
- **Java >= 8** [JDK8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [JDK11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
  - needed by Gradle 5 wrapper and... everything
- **Docker** [Mac](https://hub.docker.com/editions/community/docker-ce-desktop-mac), [Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
  - needed to run test containers in integration tests
- **Elasticsearch 5.6.14**
  - running on port 9200
- **Node**
  - needed to hot-reload client via npm/webpack-dev-server
  
```
brew install elasticsearch@5.6
brew services start elasticsearch@5.6

brew install node
```

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

##### Verify Endpoints (use `https` when enabling security features)
```
# Elasticsearch
http://localhost:9200/

# APIs
http://localhost:8098/onestop-admin/actuator/info
http://localhost:8097/onestop-search/actuator/info

# Client
# port here is automatically assigned when using webpack-dev-server
# and seen in the output of `npm run dev`
http://localhost:<port>/onestop
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

##### Verify Endpoints (use `https` when enabling security features)
```
# Elasticsearch
http://localhost:30092/

# APIs
http://localhost:30098/onestop-admin/actuator/info
http://localhost:30097/onestop-search/actuator/info

# Client
# port here is automatically assigned when using webpack-dev-server
# and seen in the output of `npm run dev`
http://localhost:30000/onestop
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

# Enabling Security Features

Due to the static nature of registering public certificates to identity providers like ICAM and login.gov, we have opted not to publish public "debug" keystores and associated metadata with these providers, as those registrations could be clobbered by a variety of unknown, unrelated developers.

It is up to the open source developer to register their own SP "Service Provider" metadata with these IdP "Identity Providers" and configure their applications accordingly, but we don't recommend this because it is probably a waste of your time. For obvious reasons, we don't accept pull requests associated with our authentication code or configuration. 

If you wish to contribute to features of our project related to or obscured by security, it is better to simply toggle Spring Profile annotations to debug against those features. Just remember to return those annotation to their original state before making a PR. The specific profiles (feature toggles) used in our project are explained below.

### Spring Profiles
...

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

### Kubernetes Secrets
Turning on security-related features requires setting up JKS (Java Key Store) keystores as a Kubernetes secrets.

Due to the sensitive nature of certs, this is a one-time setup which is done separately. See [private instructions](https://github.com/cedardevs/help/wiki/local-secure-development-setup) for how to do this setup.

##### Keystore File


##### Keystore Credentials
