### Local setup
Build with:  
`./mvnw clean package -Pnative`

Copy native executable to your start server path:  
`cp target/plexhelp-commander-1.0.0-runner your/Path`

Create config folder in your start server path:  
`mkdir your/Path/config`

Create your application.properties file (check `resources` for defaults) and copy in `config` folder:  
`cp application.properties your/Path/config`

Start server from your path (it will auto read the properties in config):  
`./plexhelp-commander-1.0.0-runner`

### Docker setup
Build with:  
`./mvnw clean package -Pnative`

Create Docker image with:  
`docker build -f src/main/docker/Dockerfile.native -t plexhelp-commander .`

Go to the folder that contains your *config* folder and run image with:
**NOTE: Host media folders need to be mounted at `/work/media/` + either `downloads` or `movies` or `tv`**
`docker run -p 9000:8080 -v ./config:/work/config -v hostDownloadsPath:/work/media/downloads -v hostMoviesPath/work/media/movies -v hostTvPath:/work/media/tv --name commander plexhelp-commander:latest`

### OpenApi
Swagger UI available at ```http://localhost:<PORT>/q/swagger-ui```
To generate API spec use: ```curl http://localhost:<PORT>/q/openapi```
