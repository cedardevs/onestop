#ONESTOP_SUBPATH
sed -i -e "s/ONESTOP_SUBPATH/${1}/g" /usr/local/apache2/conf/httpd.conf

httpd-foreground
