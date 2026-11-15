FROM eclipse-temurin:21-jdk AS build
LABEL authors="maleki"

WORKDIR /app

# Maven Wrapper
COPY .mvn/ .mvn/
COPY mvnw ./

# Parent and  Service POMs
COPY pom.xml ./
COPY middleware/pom.xml middleware/pom.xml
RUN chmod +x mvnw

# Download dependencies for Docker layer caching
RUN ./mvnw -B -pl middleware -am dependency:go-offline

# Copy only the  source code
COPY middleware/src middleware/src

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build \
    /app/middleware/target/middleware.jar \
    app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]