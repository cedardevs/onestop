<div align="center"><a href="/onestop/developer">Developer Documentation Home</a></div>
<hr>

**Estimated Reading Time: 25 minutes**
# CAS Server Development

## Table of Contents
* [Overview](#overview)
* [CAS WAR Overlay](#cas-war-overlay)
    * [REST Protocol](#rest-protocol)
    * [JSON Registry](#json-registry)
    * [Configuring CAS Server for Development](#configuring-cas-server-for-development)
    * [Registering Services](#registering-services)
    * [Building Development CAS Server from Overlay](#building-development-cas-server-from-overlay)
    * [Deploying Development CAS Server with Kubernetes and Helm](#deploying-development-cas-server-with-kubernetes-and-helm)
* [Spring Boot Configuration](#spring-boot-configuration)
    * [Dependencies](#dependencies)
    * [Spring 'cas' Profile Configuration](#spring-cas-profile-configuration)
    * [Spring Security Filter](#spring-security-filter)
    * [Configuration-Based Authorization Roles](#configuration-based-authorization-roles)
    
## Overview
The Inventory Manager ([PSI](https://github.com/cedardevs/psi)) and [OneStop](https://github.com/cedardevs/onestop) projects leverage the [CAS Server](https://apereo.github.io/cas/current/index.html) for authentication.

As of today (July 2, 2019), the federal instance of the CAS Server -- using real NEMS credentials -- is made open to any service. The login endpoint ([https://auth.ncdc.noaa.gov/cas/login](https://auth.ncdc.noaa.gov/cas/login)), however,  will eventually require individual services to be registered.


For now, while the registration is left open, it is okay to develop against this federal instance, but there are a few key disadvantages to doing this moving forward:
1. Some features may not be built into the CAS server used on `.gov` yet (e.g. - REST Protocol).
1. You are limited to testing with real NEMS credentials (or service accounts).
1. Once registration is no longer open, testing against `.gov` requires the overhead dependency of registering your service with a team (who may not be working on your same timeline) and may not have a "test" instance of CAS Server to freely add test registrations to.

## CAS WAR Overlay
The recommended approach to configuring a custom development CAS Server is to leverage their [WAR Overlay](https://apereo.github.io/cas/6.0.x/installation/WAR-Overlay-Installation.html#war-overlay-installation). This allows you to explicitly build features into the CAS Server, rather than try to kludge a Docker image by replacing configurations after-the-fact.

For our development purposes, we deploy into a Kubernetes environment using Helm charts. So our desired output from the Overlay build is a Docker image which can be referenced locally or published to our own custom DockerHub repo (once the proper dependencies and configurations have been added).

### REST Protocol
During our development, we discovered that the CAS [REST Protocol](https://apereo.github.io/cas/6.0.x/protocol/REST-Protocol.html#rest-protocol) was missing on the federal CAS Server (an ITSS request was put in to include it). The following shows how to add that dependency via the CAS overlay `build.gradle`.

```
dependencies {
    ...
    // support CAS REST Protocol
    compile "org.apereo.cas:cas-server-support-rest:${casServerVersion}"
    ...
}
```

### JSON Registry
Regardless of the federal mechanism that will be used to register CAS client services in the future, the default overlay defaults to expecting a service to be registered. Rather than simply turn this feature off (we expect it to eventually be required), we also need to modify the overlay to include the [JSON Registry](https://apereo.github.io/cas/6.0.x/services/JSON-Service-Management.html#json-service-registry) dependency into the CAS overlay.

```
dependencies {
    ...
    // support registering services via json
    compile "org.apereo.cas:cas-server-support-json-service-registry:${casServerVersion}"
    ...
}
```

### Configuring CAS Server for Development
The CAS overlay project's `etc/cas/config/cas.properties` is an important configuration built into the CAS server image. See the example additions to this file below which configure the JSON Registry, Tomcat behind a proxy (we leverage an [NGINX Kubernetes Ingress](https://github.com/kubernetes/ingress-nginx)), and additional endpoints used for development:

```
## psi-dev-cas is our k8s CAS service reference for internal traffic
cas.server.name=http://psi-dev-cas:8080
cas.server.prefix=${cas.server.name}/cas

logging.config: file:/etc/cas/config/log4j2.xml

# cas.authn.accept.users=

# initialize registry from JSON files
cas.serviceRegistry.json.location=file:/etc/cas/services

# Deploy Behind a Proxy
# https://apereo.github.io/2018/11/16/cas60-gettingstarted-overlay/#deploy-behind-a-proxy

server.port=8080
server.ssl.enabled=false
cas.server.tomcat.http.enabled=false
cas.server.tomcat.httpProxy.enabled=true
cas.server.tomcat.httpProxy.protocol=HTTP/1.1
cas.server.tomcat.httpProxy.secure=true
cas.server.tomcat.httpProxy.scheme=http
```
### Registering Services
If the JSON Registry is included and and configured properly, the `etc/cas/services/` directory is where you would include a JSON file to register your service. For example, you could add a `casSecuredApp-19991.json` file to this directory with the following contents:

```
{
    "@class" : "org.apereo.cas.services.RegexRegisteredService",
    "serviceId" : "^http",
    "name" : "PSI Registry",
    "description": "The PSI Registry uses CAS Server to add authentication to publishing endpoints.",
    "id" : 19991,
    "evaluationOrder" : 1
}
```

For development CAS, we loosely define our `serviceId` regex to be anything starting with `^http`, so that we don't need to worry about CAS denying new development services or changing endpoints.

Ideally (in production -- if and when they decide to use a more strict registration enforcement), `serviceId` here should match the `service` in the client's CAS configuration. A separate registration and ids would be needed for a service deployed to a different environment. For example, if you were developing and testing authentication on `data.noaa.gov`, you might use `"serviceId" : "^http://data.noaa.gov/onestop/api/registry/login/cas".


### Building Development CAS Server from Overlay
```
git clone https://github.com/apereo/cas-overlay-template.git
cd cas-overlay-template
# add dependencies to `build.gradle` and configuration to `etc/cas/config/cas.properties`
./gradlew jibDockerBuild
# outputs local docker image, for example:
# org.apereo.cas/cas  latest  acd035a3249f  3 days ago  338MB
```

### Deploying Development CAS Server with Kubernetes and Helm
A Helm chart should already exist in the PSI project for a development CAS, but the following outlines how it was created and modified to work alongside our existing NGINX Ingress and registry service:

```
cd ${PROJECT_HELM_CHART_DIR}
helm create cas # creates stub of helm chart
```

#### Publishing a Custom Docker Image
If you wish to publish your customised development CAS server image (built from the overlay's `jibDockerBuild` task), you can tag your own docker image using the docker image ID built from the `jibDockerBuild` task in the overlay project:

```
# re-tag the custom CAS server image built from the overlay
docker tag acd035a3249f cedardevs/cas:1.0

# login to docker hub using the `cedardeployer` credentials
docker login

# push the custom image to the cedardeployer "cas" repo
docker push cedardevs/cas:1.0
```

#### Specifying Docker Image
Knowing the docker image we want to use for our development CAS Server, we can modify the `values.yaml` of the CAS Helm chart:

```
image:
  # could be `cedardevs/cas` if you published your own image DockerHub, for example
  repository: org.apereo.cas/cas 
  tag: latest
  # ensures the locally-built image from overlay's `jibDockerBuild` task is used
  pullPolicy: IfNotPresent 
```

#### Modify Kubernertes Deployment
The default `livenessProbe` and `readinessProbe` cause issues starting the CAS Server out-of-the-box. You may want to modify them to your needs or comment them out of the `deployment.yaml` altogether:
```
#          livenessProbe:
#            httpGet:
#              path: /
#              port: http
#          readinessProbe:
#            httpGet:
#              path: /
#              port: http
```

#### Modify Kubernetes Ingress
An important note to make about the NGINX Ingress we use in Kubernetes: we needed to change it's expected backend protocol to AJP in order for it to effectively communicate with CAS Server. In the `values.yaml` for the CAS Helm chart:

```
ingress:
  ...
  annotations:
    nginx.ingress.kubernetes.io/backend-protocol: "AJP"
  ...
```

Moreover, you will need to **enable** the ingress and specify the host and path:

```
ingress:
  ...
  enabled: true
  hosts:
    - host: localhost
      paths: ["/cas"]
  ...
```

#### Feature Toggling Security
To toggle the Spring Profile, 'cas', Skaffold and/or Helm needs to know how to set the `SPRING_PROFILE_ACTIVE=cas`. Multiple profiles can be set via Skaffold and Helm if the Helm charts have the proper template to properly comma-delimit the environment variable:

```
# skaffold.yaml
deploy:
  helm:
    releases:
    - name: psi-registry
      ...
      setValues:
        # features are directly tied to `export SPRING_PROFILES_ACTIVE='cas,...'
        # all features default to "false" in base chart, unless otherwise overridden
        features.cas: true # override the default false set in the chart's `values.yaml`
```

```
# helm/psi-registry/values.yaml
# chart cas feature defaulted to false
features:
  cas: false
```

{% raw %}
```
# helm/psi-registry/templates/statefulset.yaml
...
    env:
    # EXPORT ACTIVE SPRING PROFILES TO TELL SPRING WHICH FEATURES TO ENABLE
    # the loop is making a comma delimited list for multi-feature handling
    - name: SPRING_PROFILES_ACTIVE
      value: '{{ $active := dict "profiles" (list) -}}
              {{- range $feature, $enabled := .Values.features -}}
                {{- if $enabled -}}
                  {{- $noop := $feature | append $active.profiles | set $active "profiles" -}}
                {{- end -}}
              {{- end -}}
              {{- join "," $active.profiles }}'
...
```
{% endraw %}

## Spring Boot Configuration

Please reference the classes in `org.cedar.psi.registry.security` to understand the total Spring CAS integration. This following documentation highlights some of the important pieces, but does not show the full implementation details.

### Dependencies

```
// -- CAS Authentication --
implementation('org.springframework.boot:spring-boot-starter-security')
implementation('org.springframework.security:spring-security-cas')
testImplementation('org.springframework.security:spring-security-test')
```
 
### Spring 'cas' Profile Configuration
The following is an example configuration that could be used in `application-cas.yml` (applied when the `cas` profile is active) to authenticate against the NCDC endpoint while developing on localhost.

```
cas:
  service: 'http://localhost/registry/login/cas'
  serverUrlPrefix: 'https://auth.ncdc.noaa.gov/cas'
  loginUrl: 'https://auth.ncdc.noaa.gov/cas/login'
  logoutSuccessUrl: 'https://auth.ncdc.noaa.gov/cas/logout'
  providerKey: 'CAS_PROVIDER_INVENTORY_MANAGER_NCDC'
  sendRenew: false
```

If you were leveraging your own local or development CAS server, you could instead have configurations like:

```
# local development
cas:
  service: 'localhost/onestop/api/registry/login/cas'
  serverUrlPrefix: 'http://localhost/cas'
  loginUrl: 'http://localhost/cas/login'
  logoutSuccessUrl: 'http://localhost/cas/logout'
  providerKey: 'CAS_PROVIDER_INVENTORY_MANAGER_LOCALHOST'
  sendRenew: false
```

```
# Federal development
cas:
  service: 'https://data.noaa.gov/onestop/api/registry/login/cas'
  serverUrlPrefix: 'https://data.noaa.gov/cas'
  loginUrl: 'https://data.noaa.gov/cas/login'
  logoutSuccessUrl: 'https://data.noaa.gov/cas/logout'
  providerKey: 'CAS_PROVIDER_INVENTORY_MANAGER_SCIAPPS'
  sendRenew: false
```

These values are read into the application in the `CASConfigurationProperties` class, and used to configure various beans used in the security filter.

### Spring Security Filter

The security filter defined in the `SecurityEnabledConfig` can vary greatly depending on what endpoints need to be secured and how the authorization rules are applied. In general, more specific rules should be captured first in the filter. The configuration below is a way to secure endpoints starting with `/metadata/**` against `POST`, `PUT`, `PATCH`, and `DELETE` HTTP methods. A notable exception are the secured `**/resurrection` `GET` endpoints.

The best way to think of this configuration is to ensure you capture the secure endpoints first, before falling back to public endpoints. Be careful that you do not put overly prescriptive matchers (e.g. - "/**") early in the filter as it may catch endpoints that don't require roles or even authentication at all. For example, the built-in `CasAuthenticationFilter` endpoints like `/login/cas` or `/error` could then cause an infinite redirect loop.

Because of how the Spring Security filter works, it is highly recommended to prefix your service endpoints with a common or distinguishable prefix. In this case, knowing that all the non-security related mappings begin with `/metadata` (`@RequestMapping(value = "/metadata")`). This gives you a level of control over what's secure, and makes the filter easier to follow than trying to override the built-in features of the `CASAuthenticationFilter`.

```
  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.addFilter(casAuthenticationFilter())

    http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(new AccessDeniedHandler() {
      @Override
      void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization Failed : " + accessDeniedException.getMessage())
      }
    })

    http.logout().permitAll().logoutSuccessUrl("/logout")

    http.authenticationProvider(authenticationProvider).authorizeRequests()

      // secured endpoints
      .antMatchers(HttpMethod.POST, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.PUT, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.PATCH, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.DELETE, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.GET, "**/resurrection").hasRole("ADMIN")

      .antMatchers("/login/cas","/login").authenticated()

      // everything else is publicly accessible
      .antMatchers("/**").permitAll()
  }
```

### Configuration-Based Authorization Roles

If our security filter locked down certain endpoints with `hasRole("ADMIN")`, that means, even if you've authenticated against CAS successfully, you will still be denied access to that endpoint if you aren't associated to "ROLE_ADMIN" authority (see `AuthorizationConfigurationProperties` for more details on how this simple config-based association is made for now).
```
authorization:
  roles:
    ADMIN:
      - casuser
```

In other words, if my service redirects to CAS and I login with valid credentials as "casuser", then the `UserDetailsService` set by the `CasAuthenticationProvider` will be what determines how to associate this username to granted authorities. It will see that "casuser" is associated to `ROLE_ADMIN` and the security filter will complete and redirect to my original request.

On the other hand, if I were to login with a different user that is _not_ listed under the "ADMIN" role, then I may get past the CAS login screen and redirect back to the service, but I will be given an "Access Denied" exception and still not be allowed to see the response of my original request.

<hr>
<div align="center"><a href="#">Top of Page</a></div>