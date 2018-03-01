FROM java:8-jre

ARG WAR_NAME
ARG VCS_REF
ARG VERSION
ARG DATE

LABEL org.label-schema.schema-version=1.0
LABEL org.label-schema.version=${VERSION}
LABEL org.label-schema.build-date=${DATE}
LABEL org.label-schema.name="PSI Script Wrapper"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/onestop
LABEL org.label-schema.vcs-ref=${VCS_REF}

# Install dependencies to run scripts
# NOTE: Installing groovy this way overrides the default java to be 7,
#       which then prevents our app from running. Moving on for now.
RUN apt-get update && \
    apt-get -y install python nodejs groovy

# NOTE: adding java to the path prevents the container from defaulting to 1.7
ENV PATH=$JAVA_HOME/bin:$PATH

WORKDIR /usr/src/app
COPY build/libs/${WAR_NAME} .
COPY scripts/ ./scripts
RUN ln -s ${WAR_NAME} script-wrapper.war
EXPOSE 8080
CMD ["java", "-jar", "/usr/src/app/script-wrapper.war"]
