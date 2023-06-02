package net.cserny.command;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

// TODO: add tests
@Path("/v1/commands")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommandResource {

    private static final Logger LOGGER = Logger.getLogger(CommandResource.class);

    @Inject
    LocalCommandService localCommandService;

    @POST
    public CommandResponse executeCommand(CommandRequest commandRequest) {
        LOGGER.infov("Received commandRequest request with payload {0}", commandRequest);
        return localCommandService.execute(commandRequest.name(), commandRequest.params());
    }
}
