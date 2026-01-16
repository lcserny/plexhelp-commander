package net.cserny.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MediaFileGroup;
import net.cserny.support.CommanderController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/media-searches")
public class MediaSearchController implements ApiApi {

    private final MediaSearchService service;

    @GetMapping
    @Override
    public ResponseEntity<List<MediaFileGroup>> searchMedia() {
        return ResponseEntity.ok(service.findMedia());
    }
}
