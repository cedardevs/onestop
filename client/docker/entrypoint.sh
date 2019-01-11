
sed -i -e "s/ONESTOP_PREFIX_PATH/${1}/g" /usr/local/apache2/conf/httpd.conf

httpd-foreground
