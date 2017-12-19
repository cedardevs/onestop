#!/usr/bin/env bash

sed -i '' \
    -e "s~ELASTICSEARCH_ENDPOINT~${ELASTICSEARCH_ENDPOINT:-http://elasticsearch:9200/search/collection/_search}~" \
    "${INSTALL_DIR}/WEB-INF/classes/gs/config/Config.js"

# Placed on PATH by tomcat image
catalina.sh run
