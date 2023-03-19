package net.cserny.download;

import org.jboss.resteasy.reactive.RestQuery;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;

@Path("/v1/media-downloads")
public class MediaDownloadResource {

    @Inject
    MediaDownloadService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DownloadedMedia> downloadsCompleted(@RestQuery int year, @RestQuery int month, @RestQuery int day) {
        return service.retrieveAllFromDate(LocalDate.of(year, month, day));
    }
}
