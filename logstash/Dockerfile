FROM openjdk:11

LABEL org.label-schema.name="OneStop LogStash"
LABEL org.label-schema.vendor=CEDAR
LABEL org.label-schema.vcs-url=https://github.com/cedardevs/onestop

# Install Logstash
RUN apt-get update
RUN wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -
RUN apt-get install apt-transport-https
RUN echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-6.x.list
RUN apt-get update && apt-get install logstash

WORKDIR /usr/share/logstash
COPY logstash.conf /usr/share/logstash
EXPOSE 5044
CMD ["bin/logstash", "-f", "logstash.conf"]