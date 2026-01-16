package net.cserny.qtorrent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.support.CommanderController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/torrents")
public class TorrentsController implements ApiApi {

    private final TorrentsService torrentsService;

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
