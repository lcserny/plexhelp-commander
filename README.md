Build with:
`./mvnw clean package -Pnative`

Create Docker image with:
`docker build -f src/main/docker/Dockerfile.native -t plexhelp-commander .`

Go to the folder that contains your *config* folder and run image with:
**NOTE: Host media folders need to be mounted at `/work/media/` + either `downloads` or `movies` or `tv`**
`docker run -p 9000:8080 -v ./config:/work/config -v hostDownloadsPath:/work/media/downloads -v hostMoviesPath/work/media/movies -v hostTvPath:/work/media/tv --name commander plexhelp-commander:latest`
Swagger UI available at ```http://localhost:<PORT>/q/swagger-ui```

To generate API spec use: ```curl http://localhost:<PORT>/q/openapi```
