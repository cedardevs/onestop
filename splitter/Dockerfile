FROM gcr.io/distroless/java

ARG JAR_NAME
ARG VCS_REF
ARG VERSION
ARG DATE

LABEL org.label-schema.schema-version=1.0
LABEL org.label-schema.version=${VERSION}
LABEL org.label-schema.build-date=${DATE}
LABEL org.label-schema.name="PSI Splitter"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/psi
LABEL org.label-schema.vcs-ref=${VCS_REF}

WORKDIR /usr/src/app
COPY build/libs/${JAR_NAME} splitter.jar
COPY splitter.yml /usr/src/app/splitter.yml
CMD [ "splitter.jar" ]
