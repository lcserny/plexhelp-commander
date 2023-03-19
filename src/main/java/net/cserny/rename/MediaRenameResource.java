package net.cserny.rename;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1/media-renames")
public class MediaRenameResource {

    @Inject
    MediaRenameService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RenamedMediaOptions produceRenames(MediaRenameRequest request) {
        return service.produceNames(request.name(), request.type());
    }
}
