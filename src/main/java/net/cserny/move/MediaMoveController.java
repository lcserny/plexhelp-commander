package net.cserny.move;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.*;
import net.cserny.support.CommanderController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/media-moves")
public class MediaMoveController implements ApiApi {

    private final MediaMoveService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<List<MediaMoveError>> moveMedia(@RequestBody @Valid MediaMoveRequest moveRequest) {
        return ResponseEntity.ok(service.moveMedia(moveRequest.getFileGroup(), moveRequest.getType()));
    }

    @PostMapping(value = "/all", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<List<MediaMoveError>> moveAllMedia(@RequestBody @Valid List<@Valid MediaMoveRequest> moveRequests) {
        return ResponseEntity.ok(moveRequests.stream()
                .flatMap(req -> service.moveMedia(req.getFileGroup(), req.getType()).stream())
                .toList()
        );
    }

    @GetMapping
    @Override
    public ResponseEntity<PaginatedMovedMedia> getMovedMediaPaginated(Pageable pageable) {
        Page<MovedMediaData> movedMedia = service.getAllMovedMedia(pageable);
        PaginatedBase1Page page = new PaginatedBase1Page(movedMedia.getSize(), movedMedia.getNumber(), movedMedia.getTotalElements(), movedMedia.getTotalPages());
        return ResponseEntity.ok(new PaginatedMovedMedia(page, movedMedia.getContent()));
    }
}
