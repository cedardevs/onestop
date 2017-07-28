FROM nginx:alpine

# Install curl (debugging only; remove later)
#RUN apt-get update && apt-get install -y --no-install-recommends curl \
#    && rm -rf /var/lib/apt/lists/*

EXPOSE 80

COPY dist /usr/share/nginx/html
COPY docker-nginx.conf /etc/nginx/conf.d/default.conf
