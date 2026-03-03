FROM docker.io/library/eclipse-temurin:21-jre-noble
VOLUME /tmp
COPY --chown=ubuntu:ubuntu plexhelp-commander-web/target/plexhelp-commander-*.jar /home/ubuntu/commander.jar
WORKDIR /home/ubuntu
USER ubuntu
ENTRYPOINT ["sh", "-c", "java -jar /home/ubuntu/commander.jar"]
