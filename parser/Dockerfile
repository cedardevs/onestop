FROM java:8-jre

ARG WAR_NAME
ARG VCS_REF
ARG VERSION
ARG DATE

LABEL org.label-schema.schema-version=1.0
LABEL org.label-schema.version=${VERSION}
LABEL org.label-schema.build-date=${DATE}
LABEL org.label-schema.name="PSI Parser"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/onestop
LABEL org.label-schema.vcs-ref=${VCS_REF}

# requirements for rocksdb, used by kafka streams
#RUN apk add --no-cache libbz2 libgcc libstdc++ musl snappy zlib

WORKDIR /usr/src/app
COPY build/libs/${WAR_NAME} /usr/src/app
RUN ln -s ${WAR_NAME} parser.war
EXPOSE 8080
CMD ["java", "-jar", "/usr/src/app/parser.war"]
