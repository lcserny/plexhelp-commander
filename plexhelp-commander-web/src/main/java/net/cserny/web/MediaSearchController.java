package net.cserny.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.core.search.MediaSearchService;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MediaFileGroup;
import net.cserny.support.CommanderController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/media-searches")
public class MediaSearchController implements ApiApi {

    private final MediaSearchService service;

    @GetMapping
    @Override
    public ResponseEntity<List<MediaFileGroup>> searchMedia() {
        try {
            return ResponseEntity.ok(service.findMedia());
        } catch (IOException e) {
            log.error("Error occurred while trying to search for media", e);
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
