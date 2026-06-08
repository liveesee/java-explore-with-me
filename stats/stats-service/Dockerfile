FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY lombok.config .
COPY stats stats
COPY main main
RUN mvn -B -pl stats/stats-service -am package -DskipTests

FROM amazoncorretto:21-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /app/stats/stats-service/target/stats-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
