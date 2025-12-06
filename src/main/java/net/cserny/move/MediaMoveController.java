package net.cserny.move;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MediaMoveError;
import net.cserny.generated.MediaMoveRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/media-moves",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MediaMoveController implements ApiApi {

    private final MediaMoveService service;

    @PostMapping
    @Override
    public ResponseEntity<List<MediaMoveError>> moveMedia(@RequestBody @Valid MediaMoveRequest moveRequest) {
        return ResponseEntity.ok(service.moveMedia(moveRequest.getFileGroup(), moveRequest.getType()));
    }

    @PostMapping("/all")
    @Override
    public ResponseEntity<List<MediaMoveError>> moveAllMedia(@RequestBody @Valid List<@Valid MediaMoveRequest> moveRequests) {
        return ResponseEntity.ok(moveRequests.stream()
                .flatMap(req -> service.moveMedia(req.getFileGroup(), req.getType()).stream())
                .toList()
        );
    }
}
