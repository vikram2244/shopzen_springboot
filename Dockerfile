# ===============================
# Stage 1 - Build
# ===============================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven files
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# ===============================
# Stage 2 - Runtime
# ===============================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy generated jar
COPY --from=build /app/target/*.jar app.jar

# Render provides PORT automatically
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]