FROM eclipse-temurin:21-jdk AS build
LABEL authors="maleki"

WORKDIR /app

# Maven Wrapper
COPY .mvn/ .mvn/
COPY mvnw ./

# Parent and  Service POMs
COPY pom.xml ./
COPY query-service/pom.xml query-service/pom.xml
RUN chmod +x mvnw

# Download dependencies for Docker layer caching
RUN ./mvnw -B -pl query-service -am dependency:go-offline

# Copy only the  source code
COPY query-service/src query-service/src

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build \
    /app/query-service/target/query-service.jar \
    app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]