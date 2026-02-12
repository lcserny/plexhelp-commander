package net.cserny.core.move;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.*;
import net.cserny.support.CommanderController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/media-moves")
public class MediaMoveController implements ApiApi {

    private final MediaMoveService service;

    @Override
    public ResponseEntity<List<MediaMoveError>> moveMedia(@RequestBody @Valid MediaMoveRequest moveRequest) {
        return ResponseEntity.ok(service.moveMedia(moveRequest.getFileGroup(), moveRequest.getType(), moveRequest.getMediaDesc()));
    }

    @Override
    public ResponseEntity<List<MediaMoveError>> moveAllMedia(@RequestBody @Valid List<@Valid MediaMoveRequest> moveRequests) {
        return ResponseEntity.ok(moveRequests.stream()
                .flatMap(req -> service.moveMedia(req.getFileGroup(), req.getType(), req.getMediaDesc()).stream())
                .toList()
        );
    }

    @Override
    public ResponseEntity<PaginatedMovedMedia> getMovedMediaPaginated(Pageable pageable) {
        Page<MovedMediaData> movedMedia = service.getAllMovedMedia(pageable);
        PaginatedBase1Page page = new PaginatedBase1Page(movedMedia.getSize(), movedMedia.getNumber(), movedMedia.getTotalElements(), movedMedia.getTotalPages());
        return ResponseEntity.ok(new PaginatedMovedMedia(page, movedMedia.getContent()));
    }

    @Override
    public ResponseEntity<List<MovedMediaData>> getAvailableMovedMedia() {
        return ResponseEntity.ok(service.getAvailableMovedMedia());
    }
}
