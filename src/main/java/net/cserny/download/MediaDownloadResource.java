package net.cserny.download;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;

@Path("/v1/media-downloads")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MediaDownloadResource {

    private static final Logger LOGGER = Logger.getLogger(MediaDownloadResource.class);

    @Inject
    MediaDownloadService service;

    @GET
    public List<DownloadedMedia> downloadsCompleted(@RestQuery int year, @RestQuery int month, @RestQuery int day) {
        LOGGER.infov("Received downloadsCompleted request for year {0}, month {1} and day {2}", year, month, day);
        return service.retrieveAllFromDate(LocalDate.of(year, month, day));
    }
}
