#FROM maven:3-jdk-11 AS load-dependencies
FROM maven:3.6-jdk-11 AS build


RUN mkdir /app
COPY pom.xml /app
WORKDIR /app
RUN mvn dependency:go-offline

#RUN ls -la /app
#RUN mvn dependency:resolve dependency:resolve-plugins
#COPY --from=load-dependencies /root/.m2/repository /root/.m2/repository
COPY src /app/src
#COPY pom.xml /app
#WORKDIR /app
#RUN mvn dependency:resolve dependency:resolve-plugins
RUN mvn package

FROM openjdk:11-jdk

RUN mkdir /app
#COPY test-data/node1/.actorchoreography /root/.actorchoreography
COPY --from=build /app/target /app/target

ENTRYPOINT ["java", "-jar", "/app/target/topology-1.0-SNAPSHOT.jar"]
CMD ["--help"]