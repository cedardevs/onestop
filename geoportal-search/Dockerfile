FROM tomcat:8.5.45-jdk11-openjdk

ARG VCS_REF
ARG VERSION
ARG DATE

LABEL org.label-schema.schema-version=1.0
LABEL org.label-schema.version=${VERSION}
LABEL org.label-schema.build-date=${DATE}
LABEL org.label-schema.name="OneStop CSW/OpenSearch API"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/onestop
LABEL org.label-schema.vcs-ref=${VCS_REF}

ENV TMP_DIR="/tmp/geoportal/" \
    ZIP_URL="https://github.com/Esri/geoportal-server-catalog/releases/download/v2.5.1/geoportal-search-2.5.1.zip" \
    INSTALL_DIR="/usr/local/tomcat/webapps/geoportal-search" \
    ENTRYPOINT="/usr/local/bin/entrypoint.sh"

WORKDIR $TMP_DIR
ADD $ZIP_URL geoportal-search.zip
RUN unzip geoportal-search.zip

WORKDIR $INSTALL_DIR
RUN cp $TMP_DIR/geoportal-search.war .
RUN unzip geoportal-search.war
COPY conf/WEB-INF WEB-INF
RUN rm geoportal-search.war && rm -rf $TMP_DIR

COPY ./entrypoint.sh $ENTRYPOINT
ENTRYPOINT $ENTRYPOINT
