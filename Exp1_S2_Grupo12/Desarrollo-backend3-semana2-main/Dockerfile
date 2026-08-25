# A) Etapa de build: Maven y Java viven dentro del contenedor, no en el computador del alumno.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# B) Etapa de ejecución: imagen más liviana solo para correr el jar.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p output
ENTRYPOINT ["java", "-jar", "app.jar"]
