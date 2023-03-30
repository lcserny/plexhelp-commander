Build with:
`./mvnw clean package -Pnative`

Create Docker image with:
`docker build -f src/main/docker/Dockerfile.native -t plexhelp-commander .`

Go to the folder that contains your *config* folder and run image with:
**NOTE: Host media folders need to be mounted at `/work/media/` + either `downloads` or `movies` or `tv`**
`docker run -p 9000:8080 -v ./config:/work/config -v yourHostMediaPaths:/work/media --name commander plexhelp-commander:latest`

Swagger UI available at ```http://localhost:<PORT>/q/swagger-ui```

To generate API spec use: ```curl http://localhost:<PORT>/q/openapi```
