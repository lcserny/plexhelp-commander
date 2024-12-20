FROM eclipse-temurin:21-jre-noble
VOLUME /tmp
COPY target/plexhelp-commander-*.jar commander.jar
ENTRYPOINT ["sh", "-c", "java -jar /commander.jar"]
