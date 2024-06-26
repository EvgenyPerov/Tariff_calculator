FROM eclipse-temurin:17.0.9_9-jre-ubi9-minimal

ENV JAVA_TOOL_OPTIONS='-agentlib:jdwp=transport=dt_socket,address=*:7001,server=y,suspend=n'

ARG JAR="app/target/app-1.0-SNAPSHOT.jar"

COPY $JAR /tariffCalculator.jar

ENTRYPOINT ["java","-jar"]
CMD ["tariffCalculator.jar"]
# docker build -t ru.fastdelivery:latest .
# docker run --rm --name fastdelivery-app -p 8081:8080 -d ru.fastdelivery:latest
