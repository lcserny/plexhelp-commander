package net.cserny;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    // https://quarkus.io/guides/resteasy-reactive

    // TODO
    // https://firebase.google.com/docs/admin/setup#java
    // https://github.com/holgerbrandl/themoviedbapi/

    // TODO: maybe native binary doesn't support, then just remove files...
    // Desktop desktop = Desktop.getDesktop();
    // desktop.moveToTrash(new File());

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

    @GET
    @Path("/jsonAuto")
    @Produces(MediaType.APPLICATION_JSON)
    public SomeData helloJsonAuto() {
        return new SomeData("Some name");
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