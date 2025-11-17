FROM maven:3.9.9-eclipse-temurin-21 AS builder

ENV APP_HOME="/usr/src/application"

RUN mkdir -p ${APP_HOME}

WORKDIR ${APP_HOME}
COPY pom.xml ${APP_HOME}/
COPY src ${APP_HOME}/src/
RUN mvn clean package -DskipTests


FROM amazoncorretto:21

ENV APP_HOME="/usr/src/application"

RUN mkdir -p ${APP_HOME}
WORKDIR ${APP_HOME}
COPY --from=builder ${APP_HOME}/target/*.jar ${APP_HOME}/bookkeeper.jar

ENTRYPOINT ["java", "-jar", "/usr/src/application/bookkeeper.jar", "by.bk.Bookkeeper"]
