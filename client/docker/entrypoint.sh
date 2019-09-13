# using the '@' as a pattern match delimiter because sed gets real confused when there's a '/' in our actual pattern
# see here for details: https://stackoverflow.com/questions/9366816/sed-fails-with-unknown-option-to-s-error#answer-9366940

sed -i -e "s@ONESTOP_SEARCH_API_ENDPOINT@${ONESTOP_SEARCH_API_ENDPOINT:-onestop-search:8097/onestop-search}@g" /usr/local/apache2/conf/httpd.conf

sed -i -e "s/ONESTOP_PREFIX_PATH/${ONESTOP_PREFIX_PATH}/g" /usr/local/apache2/conf/httpd.conf

mv /usr/app/client /usr/local/apache2/htdocs/${ONESTOP_PREFIX_PATH}

httpd-foreground
