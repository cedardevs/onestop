<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 25 minutes**
# Additional Developer Information
## Table of Contents
* [Skaffold](#skaffold)
* [infraInstall script](#infrainstall-script)
* [Cleanup/Troubleshooting](#cleanup-troubleshooting-steps)
    * [Clear Caches](#clear-caches)
    * [Common Cleanup Steps](#common-cleanup-steps)
    * [Resetting Kubernetes Tools](#resetting-kubernetes-tools)
* [Upload Test Metadata](#upload-test-metadata)
    * [Use the onestop-test-data Repository](#use-the-onestop-test-data-repository)
    * [Manually Upload a Specific Metadata](#upload-a-specific-metadata)
* [Feature Toggles](#feature-toggles)
    * [Keystores and Credentials](#keystores-and-credentials)
    * [Spring Profiles](#spring-profiles)
* [Gradle](#gradle)
    * [Verify Endpoints](#verify-endpoints)
* [Running ElasticSearch Locally](#running-elasticsearch-locally)
    * [Elasticsearch & Kibana Status](#elasticsearch--kibana-status)
    * [Confirm Elasticsearch can be accessed securely within the cluster](#confirm-elasticsearch-can-be-accessed-securely-within-the-cluster)
    * [Confirm Kibana can be accessed via LoadBalancer](#confirm-kibana-can-be-accessed-via-loadbalancer)

## Skaffold

Skaffold is a command line tool that facilitates continuous development for Kubernetes applications. It handles the workflow for building, pushing and deploying your application. 

It uses profiles that are defined in the skaffold.yaml to indicate which components will be started. If you reference that skaffold.yaml you will see a profile of psi. Here's the example:

    `skaffold dev -p psi`
    
If having a major problem with a scaffold object not updating can add —force=false:

    `skaffold dev --force=false`

## infraInstall script
   *  at the root of OneStop to install Elasticsearch Operator (gives your k8s cluster knowledge of Elastic CRDs)
   * Note: The `infraInstall.sh` could, theoretically, be accomplished by using the `kubectl` deploy method in Skaffold; however, Skaffold does not yet support multiple deploy types, and we are already leveraging the `jib` deploy method. See [this issue](https://github.com/GoogleContainerTools/skaffold/issues/2875#issuecomment-533737766) to track progress.
   * Note: refer [Uninstall ECK Operator](#uninstall-eck-operator) for cleaning up prior to this if you've run the infraInstall before.

## Cleanup Troubleshooting Steps

### Clear Caches
```
./gradlew clean
rm -rf ~/.gradle/caches
cd client && rm -rf node_modules/
```

### Common Cleanup Steps
```
# Clean up space on disk from leftover docker containers
docker image prune -a --force --filter "until=12h”

# Clean up leftover helm charts
rm -rf helm/onestop-dev/charts

# find and remove any requirements.lock files
find helm/ -name "*.lock"

skaffold get deployments
# remove all resources skaffold has deployed. This will throw an error if there were no releases to delete
skaffold delete

# install Elasticsearch Operator (gives your k8s cluster knowledge of Elastic CRDs) *note below*
./helm/infraInstall.sh
```

### Resetting Kubernetes Tools

#### Uninstall ECK Operator
This step simply makes removing anything ECK related from your cluster easier, including the CRDs and operator itself.

```
./helm/infraUninstall.sh
```

#### Ensure Skaffold is not running and has no running resources
```
# CTRL-C out of any terminal using skafold and double check running processes with:
ps aux | grep skaffold

# remove all resources skaffold has deployed
skaffold delete
```

#### Delete persistent volume claims (PVCs)
If you've upgraded versions or are making big breaking changes, it is generally advised to wipe-out volumes and start fresh.

**WARNING**: it is advised to only do this after ECK and stateful apps have been taken down.
```
kubectl delete pvc --all
```

#### Clean up space on disk from leftover docker containers
```
docker image prune -a --force
```

#### Helm cleanup
Delete each and every release listed by this command:
```
# list releases
helm list
```

```
# find and remove any requirements.lock files
find helm/ -name "*.lock"

# update chart repositories
helm repo update

# rebuild the charts/ directory based on the requirements.lock file
helm dependency build

# list the dependencies for the given chart
helm dependency list

# update charts/ based on the contents of requirements.yaml
helm dependency update
```

## Upload Test Metadata
To upload metadata to OneStop you can use the upload script in the [OneStop-test-data](https://github.com/cedardevs/onestop-test-data) repo or use a curl to the [Registry API](onestop/api/registry-api).

If you are an NCEI employee contact someone on the OneStop agile team for more information on what test systems are in place for you to interact with.

### Use the onestop-test-data Repository
In the [OneStop-test-data](https://github.com/cedardevs/onestop-test-data) repo there is an upload script and test collection and granule metadata. You can use the upload script to POST metadata to OneStop. If you need to POST metadata external to that repo you can follow the readme and create the expected directory structure and manifest file. Please refer to the repo's README for more information.

```
git clone git@github.com:cedardevs/onestop-test-data.git
```

If the upload is pointing to an instance of the registry API which is secured, then it will be necessary to pass user credentials.

In order for data to be published and flow all the way to the OneStop UI, the following dependencies need to be up and running:
```
                   Kafka
|________________________________________|
 registry/    \_manager_/   \_ indexer‾\   /‾search --> client
                          |‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾|       
                                   Elasticsearch
```

At a bare minimum, Kafka and the registry should be running to successfully upload.

### Manually Upload a Specific Metadata
This is not the suggested way as the [OneStop-test-data](https://github.com/cedardevs/onestop-test-data) repo has a script to do this for you.

```bash
curl -X PUT\
     -H "Content-Type: application/xml" \
     localhost/onestop/api/registry/metadata/collection \
     --data-binary @registry/src/test/resources/dscovr_fc1.xml
```

## Feature Toggles
By default, security-related features are disabled locally. This is to streamline development because security features require access to keystores with specific credentials needed for identity providers OneStop leverages.

### Keystores and Credentials
Turning on security-related features (which use request signing) requires setting up JKS (Java Key Store) keystores and the associated credentials for applications at run time.

If you are using Kubernetes, the keystores and their credentials need to be converted into Kubernetes secrets.

Due to the sensitive nature of keystores, this is a one-time setup which is done separately by those who have the proper access. If you have access and need to work on features related to security or wish to toggle on security during development, contact someone on the agile developer team. More information to come.

#### For open source developers who wish to contribute to security-toggled features:
Due to the static nature of registering public certificates to identity providers like ICAM and login.gov, we have opted not to publish public "debug" keystores and associated metadata with these providers, as those registrations could be clobbered by a variety of unknown, unrelated developers.

It is up to the open source developer to register their own SP "Service Provider" metadata with these IdP "Identity Providers" and configure their applications accordingly, but we don't recommend this because it is probably a waste of your time. For obvious reasons, we don't accept pull requests associated with our authentication code or configuration.

If you wish to contribute to parts of our project toggled by security features, it is better to simply toggle Spring Profile annotations to debug against those features. Just remember to return those annotation to their original state before making a PR. The specific profiles (feature toggles) used in our project are explained below.

### Spring Profiles
OneStop APIs are written in Spring. Currently, the APIs utilize different authentication and authorization mechanisms; nevertheless, they each utilize "Spring Profiles" to switch security-related code on and off during deployment.

OneStop leverages these profiles to enact certain feature toggles. The features available to the different APIs are documented below.

| Spring Profile | Feature Description | Default Value |
| --- | --- | --- |
| login-gov | Enables a Spring security filter to enable OpenId authentication via `login.gov`. This also triggers the `uiConfig` endpoint to show an `auth` section which indicates to the client to show a login link. Note: This feature will eventually migrate to a new `onestop-user` service with a PostgreSQL backing DB. | *false* |
| sitemap | Enables a the `/sitemap.xml` and `/sitemap/{id}.txt` public endpoints. | *false* |

#### Changing & Overriding Profiles

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
spring:
  profiles: icam
  user.roles: yourOfficalEmail:ADMIN
```
This structure essentially provides a new application-icam.yml to the application.

## Gradle
Build the whole project with `./gradlew build`

If you want to build each component:
```
./gradlew search:build
./gradlew client:build
./gradlew registry:build
./gradlew parsalyzer:build
```

You can manually startup each app. as needed (this is what skaffold does and it does the timing and keeping everything running when they fall over).

```
./gradlew registry:bootrun
./gradlew parsalyzer:run
./gradlew search:bootrun
cd client && npm run dev
```

### Verify Endpoints
```
# Elasticsearch
http://localhost:9200/
# APIs
http://localhost/onestop/api/search/actuator/info

# Client
# port here is automatically assigned when using webpack-dev-server
# and seen in the output of `npm run dev`
http://localhost:<port>/onestop
```

## Running ElasticSearch Locally
    - Minimum 7, can use brew to install.
    - Most of us don't run a local elasticsearch for a few reasons. Primarily because the ES container deletes your data when it shuts down.
    - running on port 9200
    ```
    # Once installed ES can be started this way. Make sure you change the version number as needed to match yours.
    brew services start elasticsearch@5.6
    ```

### Elasticsearch & Kibana Status
The ECK operator makes it easy to see the state of Elastic CRDs:
```
# a`HEALTH` status of "green" indicates it's ready
kubectl get elasticsearch
kubectl get kibana
```

### Confirm Elasticsearch can be accessed securely within the cluster
```
# exec into the net-tools utility pod to try to hit elasticsearch within the cluster
kubectl exec -it $(kubectl get pods -l app=net-tools --no-headers -o custom-columns=":metadata.name") -- /bin/bash

# curl with the dev credentials (`-k` trusts the self-signed cert)
curl --user "elastic:foamcat" -k https://onestop-es-http:9200/
```

### Confirm Kibana can be accessed via LoadBalancer
```
$ kubectl get svc -l common.k8s.elastic.co/type=kibana
NAME              TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
onestop-kb-http   LoadBalancer   10.99.61.173   localhost     5601:30863/TCP   64m
```
In the browser you should be able to go to `https://localhost:5601`. Because ECK defaults to using a self-signed cert, the browser will prompt you to trust it.

You can then log in with the default development credentials (defined by a `Secret` in the `elastic.yaml`):
```
user: elastic
pass: foamcat
```

```
# Elasticsearch
http://localhost:30092/

# APIs
http://localhost/onestop/api/search/actuator/info

# Client
# port here is automatically assigned when using webpack-dev-server
# and seen in the output of `npm run dev`
http://localhost:30000/onestop
# Client Ingress enabled by default, so you can also access the client at:
http://localhost/onestop
```
<hr>
<div align="center"><a href="#">Top of Page</a></div>
