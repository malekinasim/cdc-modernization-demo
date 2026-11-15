FROM maven:3.9-eclipse-temurin-21 AS build
LABEL authors="maleki"

WORKDIR /app


# Parent and  Service POMs
COPY pom.xml ./
COPY order-service/pom.xml order-service/pom.xml
COPY shipping-service/pom.xml shipping-service/pom.xml
COPY query-service/pom.xml query-service/pom.xml
COPY middleware/pom.xml middleware/pom.xml


# Download dependencies for Docker layer caching
RUN mvn -B -pl middleware -am dependency:go-offline

# Copy only the  source code
COPY middleware/src middleware/src
RUN mvn -B -pl middleware -am package -DskipTests



# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/middleware/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]