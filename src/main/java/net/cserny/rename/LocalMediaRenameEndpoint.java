package net.cserny.rename;

import net.cserny.search.MediaFileGroup;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/rename")
public class LocalMediaRenameEndpoint {

    @Path("/media")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public List<MediaFileGroup> produceRenames(MediaRenameRequest request) {
        // TODO
        return null;
    }
}
