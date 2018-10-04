FROM gcr.io/distroless/java

ARG JAR_NAME
ARG VCS_REF
ARG VERSION
ARG DATE

LABEL org.label-schema.schema-version=1.0
LABEL org.label-schema.version=${VERSION}
LABEL org.label-schema.build-date=${DATE}
LABEL org.label-schema.name="PSI Stream Manager"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/psi
LABEL org.label-schema.vcs-ref=${VCS_REF}

# NOTE: adding java to the path prevents the container from defaulting to 1.7
ENV PATH=$JAVA_HOME/bin:$PATH

WORKDIR /usr/src/app
COPY build/libs/${JAR_NAME} .
RUN ln -s ${JAR_NAME} stream-manager.jar
CMD ["java", "-jar", "/usr/src/app/stream-manager.jar"]
