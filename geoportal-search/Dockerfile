FROM tomcat:alpine

WORKDIR /tmp/geoportal
ADD https://github.com/Esri/geoportal-server-catalog/releases/download/v2.5.1/geoportal-search-2.5.1.zip geoportal-search.zip
RUN unzip geoportal-search.zip

WORKDIR /usr/local/tomcat/webapps/geoportal-search
RUN cp /tmp/geoportal/geoportal-search.war .
RUN unzip geoportal-search.war
COPY conf/WEB-INF WEB-INF
RUN rm geoportal-search.war

# Inherit CMD from tomcat image
