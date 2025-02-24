# Stage 1: Build stage (using Maven)
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /inventory

# Copy the Maven wrapper and source files
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn ./.mvn

# Ensure mvnw is executable
RUN chmod +x mvnw

# Run the Maven build command
RUN ./mvnw clean package -DskipTests

# Stage 2: Production image (using JRE)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /inventory

# Copy the built JAR file
COPY --from=builder /inventory/target/coconut-husk-inventory-*.jar inventory.jar

# Set the environment variable for the port
ENV PORT 8080
EXPOSE $PORT

# Run the application
ENTRYPOINT ["sh", "-c", "java -jar inventory.jar --server.port=$PORT"]

