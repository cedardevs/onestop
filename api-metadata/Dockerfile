FROM tomcat:latest

ARG WAR_NAME
ARG VCS_REF
ARG VERSION
ARG DATE

LABEL org.label-schema.schema-version=1.0
LABEL org.label-schema.version=${VERSION}
LABEL org.label-schema.build-date=${DATE}
LABEL org.label-schema.name="OneStop Metadata"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/onestop
LABEL org.label-schema.vcs-ref=${VCS_REF}

COPY docker/tomcat/web.xml /usr/local/tomcat/conf/
COPY docker/tomcat/server.xml /usr/local/tomcat/conf/
COPY docker/tomcat/tomcat-users.xml /usr/local/tomcat/conf/
COPY docker/tomcat/context.xml /usr/local/tomcat/webapps/manager/META-INF/

# IMPORTANT NOTE: this cannot be deployed as onestop#admin.war, because this causes an unresolvable path for liquibase / H2. If it needs to be referenced with the context path /onestop/admin, an external proxy will be necessary
ADD build/libs/${WAR_NAME} /usr/local/tomcat/webapps/onestop-admin.war

EXPOSE 8080
