package net.cserny.download;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;

@Path("/v1/downloads")
public class DownloadHistoryEndpoint {

    @Inject
    DownloadHistoryService service;

    @Path("/completed/{year}/{month}/{day}")
    @GET
    public List<DownloadedMedia> downloadsCompleted(int year, int month, int day) {
        return service.retrieveAllFromDate(LocalDate.of(year, month, day));
    }
}
