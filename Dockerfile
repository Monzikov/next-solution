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
# Копируем модель, если она была добавлена в корень проекта во время сборки
# Если модель внутри JAR (в ресурсах), эта строка не обязательна, но полезна для внешнего управления
COPY labse_model.onnx* ./
EXPOSE 88
ENTRYPOINT ["java", "-jar", "app.jar"]
