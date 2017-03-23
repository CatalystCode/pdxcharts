# Better practice is to split this into multiple containers:
#   1. build container - build JAR and copy to the second container
#   2. run container - run the JAR
# This allows for smaller images and faster execution

FROM openjdk:8-jdk

ADD . /app
WORKDIR /app

EXPOSE 8080

CMD ["./gradlew", "run"]
