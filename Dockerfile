FROM azul/zulu-openjdk-alpine:21
VOLUME /tmp
COPY target/plexhelp-commander-*.jar commander.jar
ENTRYPOINT ["sh", "-c", "java -jar /commander.jar"]
