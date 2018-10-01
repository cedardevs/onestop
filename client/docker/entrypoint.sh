#!/bin/sh

sed -i -e "s/ONESTOP_ENDPOINT/${ONESTOP_ENDPOINT:-api-search:8097}/" /etc/nginx/conf.d/default.conf
sed -i -e "s/GEOPORTAL_ENDPOINT/${GEOPORTAL_ENDPOINT:-geoportal-search:8080}/" /etc/nginx/conf.d/default.conf
sed -i -e "s/ONESTOP_ADMIN_ENDPOINT/${ONESTOP_ADMIN_ENDPOINT:-api-metadata:8098}/" /etc/nginx/conf.d/default.conf
sed -i -e "s/GOOGLE_VERIFY_OWNERSHIP_FILE/${GOOGLE_VERIFY_OWNERSHIP_FILE:google-verify-ownership-file-not-set.html}/g" /etc/nginx/conf.d/default.conf

nginx -g 'daemon off;'
