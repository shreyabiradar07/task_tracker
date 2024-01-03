# Use an official Maven runtime as the base image
FROM maven:3.8.4-openjdk-17 AS build
# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml .
COPY src ./src

# Build the Maven project
RUN mvn clean package

# Use a smaller base image for the runtime
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build stage to the runtime container
COPY --from=build /app/target/Hello-1.0-SNAPSHOT-jar-with-dependencies.jar ./app.jar

# Expose the port that your application uses
EXPOSE 8084

# Command to run the application
CMD ["java", "-jar", "app.jar"]