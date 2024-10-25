# Use the official OpenJDK image as the base
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy .env explicitly
COPY .env /app/.env
# Copy the built JAR file from your local machine to the container
COPY target/Auth-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot app runs on (default 8080)
EXPOSE 8080

# Command to run the application (includes a delay if needed)
CMD ["sh", "-c", "sleep 20 && java -jar app.jar"]
