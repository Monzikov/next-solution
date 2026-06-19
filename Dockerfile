FROM eclipse-temurin:24-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Модели (LaBSE ~0.5 ГБ, BGE-M3 ~2.2 ГБ) в образ не пакуются — они монтируются
# как volume в /app/inferences (см. docker-compose.yml). Приложение читает их по
# путям app.labse.model-path / app.bge.model-path (по умолчанию inferences/...).
EXPOSE 88
ENTRYPOINT ["java", "-jar", "app.jar"]
