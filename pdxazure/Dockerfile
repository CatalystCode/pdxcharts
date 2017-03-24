# Better practice is to split this into multiple containers:
#   1. build container - build JAR and copy to the second container
#   2. run container - run the JAR
# This allows for smaller images and faster execution

FROM openjdk:8-jdk

WORKDIR /app

COPY build/libs/pdxazure-1.0.0-SNAPSHOT.jar .
COPY newrelic.jar .
COPY newrelic.yml .

EXPOSE 8080

CMD ["java", "-D@appId=pdxazure", "-D@environment=local", "-javaagent:newrelic.jar", "-jar", "pdxazure-1.0.0-SNAPSHOT.jar"]
