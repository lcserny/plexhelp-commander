package net.cserny.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/media-searches",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class MediaSearchController {

    @Autowired
    MediaSearchService service;

    @GetMapping
    public List<MediaFileGroup> searchMedia() {
        log.info("Received searchMedia request");
        return service.findMedia();
    }
}
