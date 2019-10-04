FROM maven:3-jdk-11 AS load-dependencies

RUN mkdir /app
COPY pom.xml /app
WORKDIR /app
RUN ls -la /app
RUN mvn dependency:resolve dependency:resolve-plugins

FROM maven:3-jdk-11 AS build

COPY --from=load-dependencies /root/.m2/repository /root/.m2/repository

#COPY --from=load-dependencies /app/target /app/target
RUN ls -la /root/.m2/repository
COPY src /app/src
COPY pom.xml /app
WORKDIR /app
RUN mvn dependency:go-offline
#RUN mvn dependency:go-offline
RUN mvn package

FROM openjdk:11-jdk

RUN mkdir /app
COPY --from=build /app/target /app/target
COPY ./node1.conf /root/.actorchoreography/node.conf

ENTRYPOINT ["java", "-jar", "/app/target/topology-1.0-SNAPSHOT.jar"]
CMD ["--help"]