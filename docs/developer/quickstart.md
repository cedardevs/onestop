# Developer Guide

Table of Contents
=================
 * [Quick Start](#quick-start)
   * [Basic System Requirements](#basic-system-requirements)
   * [Clone](#clone)
   * [Build](#build)
   * [Run](#run)
   * [Verify Endpoints](#verify-endpoints)
   * [Upload Test Data](#upload-test-data)
 * [Quick Start (Kubernetes   Helm   Skaffold)](#quick-start-kubernetes--helm--skaffold)
   * [System Requirements](#system-requirements)
   * [If running client via Node](#if-running-client-via-node)
   * [Verify Endpoints](#verify-endpoints-1)
   * [Upload Test Data](#upload-test-data-1)
   * [Making Helm Chart Changes](#making-helm-chart-changes)
   * [Troubleshooting](#troubleshooting)
   * [Clear Caches](#clear-caches)
   * [Resetting Kubernetes Tools](#resetting-kubernetes-tools)
 * [Feature Toggles](#feature-toggles)
   * [Keystores and Credentials](#keystores-and-credentials)
   * [Spring Profiles](#spring-profiles)
     * [admin](#admin)
     * [search](#search)
   * [Changing &amp; Overriding Profiles](#changing--overriding-profiles)

## Quick Start
### Basic System Requirements
- **Java >= 8** [JDK8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [JDK11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
  - needed by Gradle 5 wrapper and... everything
- **Docker** [Mac](https://hub.docker.com/editions/community/docker-ce-desktop-mac), [Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
  - needed to run test containers in integration tests
- **Elasticsearch 5.6.14**
  - running on port 9200
  - `brew install elasticsearch@5.6`
  - `brew services start elasticsearch@5.6`
- **Node**
  - needed to hot-reload client via `npm` / `webpack-dev-server`
  - `brew install node`

### Clone
`git clone https://github.com/cedardevs/onestop.git`
### Build
<details open>
  <summary>
    <code>./gradlew build</code>
  </summary>
  <br/>
  <p>For individual components:</p>
<pre>./gradlew admin:build
./gradlew search:build
./gradlew client:build</pre>
</details>

### Run
```
./gradlew admin:bootrun
./gradlew search:bootrun
cd client && npm run dev
```

### Verify Endpoints
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
# The default port is 30098 which is used for Kubernetes development
./gradlew uploadTestData --apiAdminPort=8098
```

## Quick Start (Kubernetes + Helm + Skaffold)
### System Requirements
- [Basic System Requirements](#basic-system-requirements) listed above
  - *excluding* local Elasticsearch installation
- Kubernetes
  - our team enables Kubernetes with Docker Desktop (see: `Preferences...` > `Kubernetes`)
  - we highly recommend allocating >= 6.0 GiB to Docker (see: `Preferences...` > `Advanced`)
  - some of us get even better performance by allocating even more memory and swap memory
- Helm
- Skaffold

```
brew install kubernetes-helm
brew install skaffold

# install tiller onto the cluster (one-time deal, unless upgrading helm)
helm init

# run (requires having run a `./gradlew build` for the Docker images referenced in skaffold)
skaffold dev -f skaffold.yaml
```

**WARNING**: there's a good chance the `integrationTask` gradle task will fail with less powerful machines
due to elasticsearch being a memory hog. This can often manifest in the build reporting (e.g. - `build/reports/integrationTests/index.html`) misleading errors like this:

`java.lang.NullPointerException: Cannot get property 'took' on null object`

If this resource issue is getting in your way, you can always skip tasks in gradle using the `-x integrationTest` option. Of course, this is only a quick fix, and is not acceptable for validating the success of our continuous integration builds.

### If running client via Node
```
# 1) comment out client sections in skaffold.yaml

# 2) tell webpack-dev-server proxy to use the k8s exposed port for the search API
cd client && npm run kub

# 3) run skaffold without automatic port forwarding
skaffold dev --port-forward=false -f skaffold.yaml
```

### Verify Endpoints
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
# Client Ingress enabled by default, so you can also access the client at:
http://localhost/onestop
```

### Upload Test Data
```
# The default port is 30098 which is used for Kubernetes development
./gradlew uploadTestData --apiAdminPort=8098
```

### Making Helm Chart Changes
```
# Helm helper script for updates
./helm/updateRelease.sh
```

When do I need to run this script?
- Any time a change is made to a `requirements.yaml` file
- Any time a change is made to one of the OneStop sub charts that needs to be deployed. Make sure to update the chart version appropriately in `helm/onestop/Chart.yaml` before running this script.

### Troubleshooting
Something is really messed up in my environment, what should I do?

If you find yourself in a weird state, even after freshly cloning the project, you might try some or all of the following techniques to reset your environment before building and running:

##### Clear Caches
```
./gradlew clean
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
helm delete onestop-admin --purge
helm delete onestop-search --purge
helm delete onestop-client --purge

skaffold delete
```

## Feature Toggles
By default, security-related features are disabled locally. This is to streamline development because security features require access to keystores with specific credentials needed for identity providers OneStop leverages.

### Keystores and Credentials
Turning on security-related features (which use request signing) requires setting up JKS (Java Key Store) keystores and the associated credentials for applications at run time.

If you are using Kubernetes, the keystores and their credentials need to be converted into Kubernetes secrets.

Due to the sensitive nature of keystores, this is a one-time setup which is done separately by those who have the proper access. If you have access and need to work on features related to security or wish to toggle on security during development, see our [private instructions](https://github.com/cedardevs/help/wiki/local-secure-development-setup).

<details>
   <summary>Note to open source developers who wish to contribute to security-toggled features...</summary>
   <br/>
   <p>Due to the static nature of registering public certificates to identity providers like ICAM and login.gov, we have opted not to publish public "debug" keystores and associated metadata with these providers, as those registrations could be clobbered by a variety of unknown, unrelated developers.</p>

   <p>It is up to the open source developer to register their own SP "Service Provider" metadata with these IdP "Identity Providers" and configure their applications accordingly, but we don't recommend this because it is probably a waste of your time. For obvious reasons, we don't accept pull requests associated with our authentication code or configuration.</p>

   <p>If you wish to contribute to parts of our project toggled by security features, it is better to simply toggle Spring Profile annotations to debug against those features. Just remember to return those annotation to their original state before making a PR. The specific profiles (feature toggles) used in our project are explained below.</p>
</details>

### Spring Profiles
OneStop APIs are written in Spring. Currently, the APIs utilize different authentication and authorization mechanisms; nevertheless, they each utilize "Spring Profiles" to switch security-related code on and off during deployment.

OneStop leverages these profiles to enact certain feature toggles. The features available to the different APIs are documented below.

##### admin

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| <pre><code>icam</code></pre> | Enables a Spring security filter to require ICAM CAC authentication and authorization to hit particular endpoints. | *false* |
| <pre><code>manual-upload</code></pre> | Enables the `UploadController` which opens browser endpoints for manual metadata upload. This feature should always be set with the `icam` profile in production to ensure manual upload is CAC secured. | *false* |
| <pre><code>kafka-ingest</code></pre> | Enables the `KafkaConsumerService` to upload metadata via PSI. This feature should never be enabled at the same time as the `manual-upload` feature as they are mutually exclusive approaches to metadata upload.  | *false* |
| <pre><code>sitemap</code></pre> | Enables the `SitemapETLService` to create the sitemap index and periodically refresh it. | *false* |

##### search

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| <pre><code>login-gov</code></pre> | Enables a Spring security filter to enable OpenId authentication via `login.gov`. This also triggers the `uiConfig` endpoint to show an `auth` section which indicates to the client to show a login link. Note: This feature will eventually migrate to a new `onestop-user` service with a PostgreSQL backing DB. | *false* |
| <pre><code>sitemap</code></pre> | Enables a the `/sitemap.xml` and `/sitemap/{id}.txt` public endpoints. | *false* |

### Changing & Overriding Profiles

If you are deploying without Kubernetes, Helm, and Skaffold, and running an API directly with `./gradlew search:bootrun` (for example), then you can toggle active profiles with an environment variable:

`export SPRING_PROFILES_ACTIVE=login-gov`

If there were any profiles defaulted to `true`, you would need to make sure they are listed in this env var unless you actually wanted that profile disabled. The envrionment variable expects a comma-delimited list when there are multiple active profiles.

Otherwise, using Skaffold, these environment variables are managed for you. You simply need to toggle the features listed in the `skaffold.yaml` file under the `deploy.helm.releases` section where it applies. For example:

```
- name: onestop-search
  ...
  setValues:
    features.login-gov: true
  ...
```

There is currently no configmap in the `onestop-admin` deployment to make changes to the security configuration (such as adding yourself as an admin). If you need to add one, see the search API config for how to add a configmap. The configuration needed is:
```
---
spring:
  profiles: icam
  user.roles: yourOfficalEmail:ADMIN
```
This structure essentially provides a new application-icam.yml to the application.
