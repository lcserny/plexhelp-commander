package net.cserny.search;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/v1/search")
public class LocalMediaSearchEndpoint {

    @Inject
    LocalMediaSearchService service;

    // TODO
}
