package net.cserny.rename;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1/rename")
public class MediaRenameEndpoint {

    @Inject
    MediaRenameService service;

    @Path("/media")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public RenamedMediaOptions produceRenames(MediaRenameRequest request) {
        return service.produceNames(request.name(), request.type());
    }
}
