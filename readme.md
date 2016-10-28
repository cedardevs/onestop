# OneStop Search

[![Build Status](https://travis-ci.org/cires-ncei/onestop.svg?branch=master)](https://travis-ci.org/cires-ncei/onestop)

OneStop search software is used to allow discovery and access to the 
National Oceanic and Atmospheric Administration’s publicly available 
data holdings. It is being developed on a grant by a team of researchers
from the University of Colorado (more legal info below).

## Disclaimer

This software is in an early stage of development. Only development snapshot artifacts have been built to date.

## System Information

### Deployment Overview, Dependencies

The OneStop system is comprised of:

- Back end services built on Spring Boot and written in Groovy
- An HTML/CSS/JavaScript client built on Redux and React.

Deploying it requires:

- Back end:
    - Java 8
    - Network connectivity to a node in an ElasticSearch cluster
- Front end:
    - Web server to serve static content
    - A web server proxy on the client's host from the path `/onestop/api` to the API app, `[api host machine]:8097/onestop/api` by default.

As an example, here is how you could install the entire system on a single Fedora/RedHat/CentOS machine:

1. Install Java 8 (Oracle Java works as well)

    ```
    yum install java-1.8.0-openjdk.x86_64
    ```

1. Install and start ElasticSearch

    ```shell
    yum install elasticsearch
    systemctl start elasticsearch
    ```

1. Download and start the latest API application snapshot:

    ```shell
    curl https://oss.jfrog.org/oss-snapshot-local/cires/ncei/onestop/onestop-api/0.1.0-SNAPSHOT/onestop-api-0.1.0-SNAPSHOT.jar > /usr/local/bin/onestop-api.jar
    chmod +x /usr/local/bin/onestop-api.jar
    ln -s /usr/local/bin/onestop-api.jar /etc/init.d/onestop-api
    systemctl start onestop-api
    ```

1. Host the latest client application snapshot with your favorite web server:

    ```shell
    curl https://oss.jfrog.org/oss-snapshot-local/cires/ncei/onestop/onestop-client/0.1.0-SNAPSHOT/onestop-client-0.1.0-SNAPSHOT.tar | tar -x -C /usr/share/nginx/html
    systemctl restart nginx
    ```

1. Add a proxy for the API application to your web server, e.g. in nginx add something like:
    ```shell
    location /onestop/api/search {
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_pass http://127.0.0.1:8097/onestop/api/search;
    }
    ```
    
You should now be able to hit your web server, see the UI, and execute search queries (probably with no results yet).
For more detailed information about each component, see the sections below.

### Building from Source

You can build the software from source by running `./gradlew build` from the root directory of the code.

This will use Gradle to download the necessary dependencies, run the tests, and compile the software.
The API and client artifacts will be located at `api/build/libs/onestop-api-[version].war` and `client/build/libs/onestop-client-[version].tar`, respectively.

### API

##### Installation
The API is a Spring Boot application that is packaged as a fully executable war in the api/build/libs folder,
meaning that it can be run in a standalone webapp container like tomcat, or with `java -jar onestop-api.war`, or executed directly like `./onestop-api.war`.

Probably the simplest way to install the api on a machine is to set it up as an `init.d` or `systemd` service as described in the [Spring Boot service installation guide](http://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-service).
For example, linking the application into `init.d` like `ln -s [path to jar] /etc/init.d/onestop-api` will cause the app to run on startup,
allow you to control it with tools like `service` or `systemctl` using the typical `start`, `stop`, `restart`, and `status` commands,
and pipes the logging (which uses stdout by default) to `/var/log/onestop-api.log`.

##### Dependencies
The API requires Java 8 to run and must be able to connect to ElasticSearch

##### Security
Currently the API app does not implement any authentication or authorization of its own. We recommend that you
control access to the app's sensitive endpoints with a reverse proxy. As described in the client dependencies
section below, the only endpoint required for the client to function is `/onestop/api/search`.

If your ElasticSearch cluster has the Shield security plugin installed, the API can be configured to connect to
it using two sets of credentials for readonly and read/write operations, respectively. The following is a breakdown
of the privileges required by each user. See the configuration settings below for a description of how to provide
the app with the credentials for the two users.

Readonly User:
```json
{
  "cluster": [ "transport_client" ], 
  "indices": [ 
    {
      "names": [ "search_*" ], 
      "privileges": [ "read" ] 
    }
  ] 
}
```

Read/Write User:
```json
{
  "cluster": [ "transport_client", "manage" ], 
  "indices": [ 
    {
      "names": [ "search_*", "staging_*" ], 
      "privileges": [ "all" ] 
    }
  ] 
}
```

Note: an easy way to configure the users with these privileges is to use the roles that come out of the box with Shield:

```
Readonly roles  : user, transport_client
Read/Write roles: power_user
```

##### Configuration
There are several configurable values that can be passed to the application.
They can be provided to the app in a number of ways as outlined in the [Spring Externalized Configuration Documentation](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

We recommend using a [Spring Cloud Config Server](https://cloud.spring.io/spring-cloud-config/) to make the config
available to the app in a consistent and secure way. Alternatively, a simple way to pass config values is to create
a .yml or .properties file and provide its path to the app by setting the `spring.config.location` value,
e.g. with an environment variable:

`SPRING_CONFIG_LOCATION=file:/my/config/path/config.yml ./onestop-api.war`

See the [API's default config values](api/defaults.yml) for a full picture of the values that can be set. 

### Client

##### Installation
The client is a a pure HTML/JavaScript/CSS application packaged as a tarball. To host it simply expand the tar into the root of your web server.

Note that the app requires NodeJS to be built from source (as it is concatenated, minified, compressed, etc.)
but it has no dependency on node at runtime.

##### Dependencies
The client assumes that it can make requests to `/onestop/api/search` in order to execute searches.
Hence, a reverse proxy must be put into place from that path to the api application.
For example, if you are hosting the client using `httpd` and the api application is running
on the same host, you could add the following to your `httpd.conf`:

```
ProxyPass        /onestop/api/search http://localhost:8097/onestop/api/search
ProxyPassReverse /onestop/api/search http://localhost:8097/onestop/api/search
```

Note that the API application has other endpoints, e.g. for uploading metadata and triggering indexing,
But that these endpoint are potentially destructive and should likely not be exposed to the public.
The only endpoint that the client uses (so far) is the search endpoint.

##### Configuration
The client has no configuration of its own.

### Administration

##### Uploading Metadata
The system stores ISO XML metadata records in order to power its search results.
All metadata documents must have a `<gmd:fileIdentifier>` tag containing either a
`<gco:CharacterString>` or a `<gmx:Anchor>` tag with the identifier. For example:

```
<?xml version="1.0" encoding="UTF-8"?>
<gmi:MI_Metadata xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi">
  ...
  <gmd:fileIdentifier>
    <gco:CharacterString>[IDENTIFIER]</gco:CharacterString>
  </gmd:fileIdentifier>
  ... 
</gmi:MI_Metadata>  
```

Optionally, the record can also have a `<gmd:parentIdentifier>` tag (also containing
either a `<gco:CharacterString>` or a `<gmx:Anchor>` tag) to indicate that the record is
a child of another. In this case, the `parentIdentifier` of the child record must match
the `fileIdentifier` of the parent record verbatim. For example:

```
<?xml version="1.0" encoding="UTF-8"?>
<gmi:MI_Metadata xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi">
  ...
  <gmd:fileIdentifier>
    <gco:CharacterString>[CHILD'S FILE IDENTIFIER]</gco:CharacterString>
  </gmd:fileIdentifier>
  <gmd:parentIdentifier>
    <gco:CharacterString>[PARENT'S FILE IDENTIFIER]</gco:CharacterString>
  </gmd:parentIdentifier>
  ... 
</gmi:MI_Metadata>  
```

These documents can be uploaded, retrieved, and deleted from the system using REST-style
requests around the `/onestop/api/metadata` resource endpoint.

HTTP Method | Endpoint                                  | Body      | Action
------------|-------------------------------------------|-----------|--------------------------
POST        | /onestop/api/metadata                     | ISO XML   | Upload a metadata record <sup>[1](#postfootnote)</sup>
GET         | /onestop/api/metadata/[fileIdentifier]/   | (none)    | Retrieve a metadata record <sup>[2](#getfootnoe)</sup>
DELETE      | /onestop/api/metadata/[fileIdentifier]/   | (none)    | Delete a metadata record <sup>[2](#getfootnoe)</sup>

- <a href="postfootnote">1</a>: Note that POSTing an XML record with the same fileIdentifier as a previously-POSTed record will result in replacing that record.
- <a href="getfootnote">2</a>: The trailing `/` in URLs which include fileIdentifiers is important if the fileIdentifier includes any `.` characters.

##### Indexing Metadata for Search
Some sample metadata load processes are available.  For example, on the command line you can run:

>gradle etlDEMToLocalApi

Once some metadata documents have been uploaded into the system, you can trigger the indexing process to
make them searchable. This process will correlate child records with their parents and index them such that
any indexed values provided in the child will override those in the parent, while any values not present in
the child will be inherited from the parent.

The indexing can be triggered by sending an emtpy `GET` or `PUT` request to `/onestop/api/admin/reindex`

##### Verifying Search

Finally, after standing up the system, uploading some metadata, and indexing it, you can verify that the system
works by visiting the hosted client in your browser and running a search, e.g. for the value of a `<gmd:keyword>`
tag in one or more of the uploaded metadata documents.

## Legal

This software was developed by Team Foam-Cat, 
under the OneStop project: 1553647, 
NOAA award number NA12OAR4320127 to CIRES.
This code is licensed under GPL version 2. 
© 2016 The Regents of the University of Colorado.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation version 2
of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
