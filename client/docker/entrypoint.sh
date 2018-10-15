#!/bin/sh

sed -i -e "s/ONESTOP_ENDPOINT/${ONESTOP_ENDPOINT:-api-search:8097}/" -e "s/GEOPORTAL_ENDPOINT/${GEOPORTAL_ENDPOINT:-geoportal-search:8080}/" -e "s/ONESTOP_ADMIN_ENDPOINT/${ONESTOP_ADMIN_ENDPOINT:-api-metadata:8098}/" -e "s/GOOGLE_VERIFY_OWNERSHIP_FILE/${GOOGLE_VERIFY_OWNERSHIP_FILE:-google-verify-ownership-file-not-set.html}/" /etc/nginx/conf.d/default.conf

nginx -g 'daemon off;'
