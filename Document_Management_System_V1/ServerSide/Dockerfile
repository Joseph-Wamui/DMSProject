# Use the official Maven image to build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy application source
COPY src ./src
RUN mvn clean package -DskipTests

# Use Eclipse Temurin for the runtime image
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Copy the wait-for-db script into the container
COPY wait-for-db.sh /usr/local/bin/wait-for-db.sh
RUN chmod +x /usr/local/bin/wait-for-db.sh

# Expose the application port (match application.properties)
EXPOSE 6300

# Pass environment variables for flexibility
ENV SPRING_DATASOURCE_URL jdbc:mariadb://db:3306/dmsproject?allowPublicKeyRetrieval=true&useSSL=false
ENV SPRING_DATASOURCE_USERNAME root
ENV SPRING_DATASOURCE_PASSWORD your_password

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
