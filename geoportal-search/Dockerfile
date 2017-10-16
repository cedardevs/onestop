FROM tomcat:alpine

ENV TMP_DIR="/tmp/geoportal/" \
    INSTALL_DIR="/usr/local/tomcat/webapps/geoportal-search/" \
    ZIP_URL="https://github.com/Esri/geoportal-server-catalog/releases/download/v2.5.1/geoportal-search-2.5.1.zip"

WORKDIR $TMP_DIR
ADD $ZIP_URL geoportal-search.zip
RUN unzip geoportal-search.zip

WORKDIR $INSTALL_DIR
RUN cp $TMP_DIR/geoportal-search.war .
RUN unzip geoportal-search.war
COPY conf/WEB-INF WEB-INF
RUN rm geoportal-search.war && rm -rf $TMP_DIR

# Inherit CMD from tomcat image
