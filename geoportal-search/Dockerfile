FROM tomcat:8.5.45-jdk11-openjdk

ARG VCS_REF
ARG VERSION
ARG DATE

LABEL org.opencontainers.image.version=${VERSION}
LABEL org.opencontainers.image.created=${DATE}
LABEL org.opencontainers.image.ref.name="OneStop CSW/OpenSearch API"
LABEL org.opencontainers.image.vendor=CEDAR
LABEL org.opencontainers.image.source=https://github.com/cedardevs/onestop
LABEL org.opencontainers.image.revision=${VCS_REF}

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
