package net.cserny.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MediaFileGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/media-searches", produces = MediaType.APPLICATION_JSON_VALUE)
public class MediaSearchController implements ApiApi {

    @Autowired
    private final MediaSearchService service;

    @GetMapping
    @Override
    public ResponseEntity<List<MediaFileGroup>> searchMedia() {
        return ResponseEntity.ok(service.findMedia());
    }
}
