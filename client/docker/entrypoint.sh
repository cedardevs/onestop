
sed -i -e "s/ONESTOP_SEARCH_API_ENDPOINT/${ONESTOP_SEARCH_API_ENDPOINT:-onestop-api-search:8097}/" /usr/local/apache2/conf/httpd.conf
sed -i -e "s/ONESTOP_PREFIX_PATH/${ONESTOP_PREFIX_PATH}/g" /usr/local/apache2/conf/httpd.conf
mv /usr/app/client /usr/local/apache2/htdocs/${ONESTOP_PREFIX_PATH}
httpd-foreground
