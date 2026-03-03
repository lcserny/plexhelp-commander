FROM docker.io/library/eclipse-temurin:21-jre-noble
VOLUME /tmp
COPY plexhelp-commander-web/target/plexhelp-commander-*.jar commander.jar

RUN useradd -m commander
USER commander

ENTRYPOINT ["sh", "-c", "java -jar /commander.jar"]
