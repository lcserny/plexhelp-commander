package net.cserny.move;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/media-moves")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MediaMoveResource {

    private static final Logger LOGGER = Logger.getLogger(MediaMoveResource.class);

    @Inject
    MediaMoveService service;

    @POST
    public List<MediaMoveError> moveMedia(MediaMoveRequest moveRequest) {
        LOGGER.infov("Received moveMedia request with payload {0}", moveRequest);
        return service.moveMedia(moveRequest.fileGroup(), moveRequest.type());
    }
}
