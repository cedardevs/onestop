#!/bin/sh

sed -i -e "s/ONESTOP_CLIENT_ENDPOINT/${ONESTOP_CLIENT_ENDPOINT:onestop-client:8097}/" /etc/nginx/conf.d/default.conf
sed -i -e "s/ONESTOP_SEARCH_API_ENDPOINT/${ONESTOP_SEARCH_API_ENDPOINT:onestop-api-search:8097}/" /etc/nginx/conf.d/default.conf
sed -i -e "s/ONESTOP_ADMIN_API_ENDPOINT/${ONESTOP_ADMIN_API_ENDPOINT:onestop-api-metadata:8098}/" /etc/nginx/conf.d/default.conf
sed -i -e "s/GEOPORTAL_ENDPOINT/${GEOPORTAL_ENDPOINT:onestop-geoportal-search:8080}/" /etc/nginx/conf.d/default.conf
sed -i -e "s/GOOGLE_VERIFY_OWNERSHIP_FILE/${GOOGLE_VERIFY_OWNERSHIP_FILE:google-verify-ownership-file-not-set.html}/g" /etc/nginx/conf.d/default.conf

nginx -g 'daemon off;'
