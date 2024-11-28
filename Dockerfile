FROM maven:3.9.8-amazoncorretto-21 AS builder

ENV APP_HOME="/usr/src/application"

RUN mkdir -p ${APP_HOME}

WORKDIR ${APP_HOME}
COPY pom.xml ${APP_HOME}/
COPY backend ${APP_HOME}/backend/
COPY frontend ${APP_HOME}/frontend/
RUN mvn clean install


FROM amazoncorretto:21

ENV APP_HOME="/usr/src/application"

RUN mkdir -p ${APP_HOME}
WORKDIR ${APP_HOME}
COPY --from=builder ${APP_HOME}/backend/target/*.jar ${APP_HOME}/bookkeeper.jar

ENTRYPOINT ["java", "-jar", "/usr/src/application/bookkeeper.jar", "by.bk.Bookkeeper"]
