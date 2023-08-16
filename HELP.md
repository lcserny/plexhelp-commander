### Local setup
Build with:  
`./mvnw clean package -Pnative`

Copy native executable to your start server path:  
`cp target/plexhelp-commander your/Path`

Create config folder in your start server path:  
`mkdir your/Path/config`

Create your application.properties file (check `resources` for defaults) and copy in `config` folder:  
`cp application.properties your/Path/config`

Start server from your path (it will auto read the properties in config):  
`./plexhelp-commander`

### TODO: OpenApi generator

### DEPRECATED: OpenApi
Swagger UI available at ```http://localhost:<PORT>/q/swagger-ui```
To generate API spec use: ```curl http://localhost:<PORT>/q/openapi```
