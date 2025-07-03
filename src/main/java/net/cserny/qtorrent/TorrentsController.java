package net.cserny.qtorrent;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/torrents")
public class TorrentsController implements ApiApi {

    @Autowired
    private TorrentsService torrentsService;

    @PostMapping
    @Override
    public ResponseEntity<Void> upsertTorrent(@Valid @RequestParam("operation") String operation,
                                              @Valid @RequestParam("hash") String hash) {
        TorrentOperation torrentOperation = TorrentOperation.fromString(operation);
        HttpStatus status = switch (torrentOperation) {
            case ADDED -> {
                torrentsService.addTorrent(hash);
                yield HttpStatus.CREATED;
            }
            case DOWNLOADED -> {
                torrentsService.downloadTorrent(hash);
                yield HttpStatus.OK;
            }
        };
        return new ResponseEntity<>(status);
    }
}
