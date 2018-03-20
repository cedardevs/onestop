FROM java:8-jre
COPY kafka-manager-1.3.3.16 ./kafka-manager-1.3.3.16
COPY application.conf /kafka-manager-1.3.3.16/conf/application.conf
ENV KM_CONTEXT_PATH='/kafka-manager/'
EXPOSE 9000
CMD ["/kafka-manager-1.3.3.16/bin/kafka-manager", "-Dconfig.file=/kafka-manager-1.3.3.16/conf/application.conf"]
