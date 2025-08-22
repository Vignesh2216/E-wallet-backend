# Step 1: Use official Maven image to build the app
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (to leverage Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Step 2: Run the app with JDK
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render expects PORT env variable)
EXPOSE 8080

# Start app
ENTRYPOINT ["java","-jar","app.jar"]
