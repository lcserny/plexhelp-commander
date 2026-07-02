FROM docker.io/library/eclipse-temurin:21-jre-noble
VOLUME /tmp
RUN mkdir -p /app && chown -R ubuntu:ubuntu /app
COPY --chown=ubuntu:ubuntu plexhelp-commander-web/target/plexhelp-commander-*.jar /app/commander.jar
WORKDIR /app
USER ubuntu
ENTRYPOINT ["sh", "-c", "java -jar /app/commander.jar"]
