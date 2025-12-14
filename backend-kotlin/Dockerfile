FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace/app

COPY . /workspace/app
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre
VOLUME /tmp
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
