Commits in rustver to bring here:
- 669bdce630624bd50b6b0569dd2e693d66b5d84c
- 888383b3f46cd78ad667914eba5dc5918d3cf53a

Then push tag 1.0





### Local setup
Build with:  
`./mvnw -Pnative clean package native:compile`

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
