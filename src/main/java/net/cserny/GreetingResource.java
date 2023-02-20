package net.cserny;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String helloJson() {
        Jsonb build = JsonbBuilder.create();
        return build.toJson(new SomeData("Some name"));
    }

    public static class SomeData {

        public String name;

        public SomeData() {
        }

        public SomeData(String name) {
            this.name = name;
        }
    }
}