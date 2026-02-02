# Stage 1: Build dự án với Maven
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng với JRE nhẹ
FROM amazoncorretto:21-alpine
WORKDIR /app
# Copy file jar đã build từ Stage 1 sang
COPY --from=build /app/target/*.jar app.jar
# Mở cổng 8080 cho IAM Service
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]