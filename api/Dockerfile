FROM openjdk:8
WORKDIR /usr/src/app
COPY build/libs/onestop-api-all-1.1.1-SNAPSHOT.war /usr/src/app
EXPOSE 8097
CMD ["java", "-jar", "/usr/src/app/onestop-api-all-1.1.1-SNAPSHOT.war", "--elasticsearch.host=elasticsearch"]