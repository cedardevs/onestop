FROM nginx:alpine

# Update
EXPOSE 80

COPY dist /usr/share/nginx/html
COPY docker-nginx.conf /etc/nginx/conf.d/default.conf