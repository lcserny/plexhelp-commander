FROM docker.io/library/eclipse-temurin:21-jre-noble
VOLUME /tmp
RUN useradd -m commander
COPY --chown=commander:commander plexhelp-commander-web/target/plexhelp-commander-*.jar /home/commander/commander.jar
WORKDIR /home/commander
USER commander
ENTRYPOINT ["sh", "-c", "java -jar /home/commander/commander.jar"]
