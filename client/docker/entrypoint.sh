#!/bin/sh

sed -i '' -e "s/UPSTREAM_HOST/${UPSTREAM_HOST:-api-search}/" -e "s/UPSTREAM_PORT/${UPSTREAM_PORT:-8097}/" /etc/nginx/conf.d/default.conf

nginx -g 'daemon off;'
