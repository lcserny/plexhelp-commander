package net.cserny.search;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/search")
public class LocalMediaSearchEndpoint {

    @Inject
    LocalMediaSearchService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<MediaFile> searchMedia() {
        return service.findMedia();
    }
}
