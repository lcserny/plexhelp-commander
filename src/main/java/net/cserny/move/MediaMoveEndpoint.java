package net.cserny.move;

import net.cserny.search.MediaFileGroup;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/move")
public class MediaMoveEndpoint {

    @Inject
    MediaMoveService service;

    @Path("/media")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public List<MediaMoveError> moveMedia(MediaMoveRequest moveRequest) {
        return service.moveMedia(moveRequest.fileGroup(), moveRequest.type());
    }
}
