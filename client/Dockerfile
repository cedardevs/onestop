FROM httpd:latest

ARG VCS_REF
ARG VERSION
ARG DATE
ARG TAR_NAME
ARG PREFIX_PATH
#ENV ENV_PREFIX_PATH=$PREFIX_PATH
#ENV ONESTOP_SEARCH_API_ENDPOINT
# Install curl (debugging only; remove later)
#RUN apt-get update && apt-get install -y --no-install-recommends curl \
#    && rm -rf /var/lib/apt/lists/*

LABEL org.label-schema.schema-version=1.0
LABEL org.label-schema.version=${VERSION}
LABEL org.label-schema.build-date=${DATE}
LABEL org.label-schema.name="OneStop Client"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/onestop
LABEL org.label-schema.vcs-ref=${VCS_REF}

EXPOSE 80

#ADD build/libs/${TAR_NAME} /usr/local/apache2/htdocs/${PREFIX_PATH}
ADD build/libs/${TAR_NAME} /usr/app/client

COPY docker/httpd.conf /usr/local/apache2/conf/httpd.conf

WORKDIR /usr/app
COPY docker/entrypoint.sh /usr/app
#ENTRYPOINT sh /usr/app/entrypoint.sh ${ENV_PREFIX_PATH}
ENTRYPOINT sh /usr/app/entrypoint.sh
