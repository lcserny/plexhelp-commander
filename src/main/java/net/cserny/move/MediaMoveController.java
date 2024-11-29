package net.cserny.move;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MediaMoveError;
import net.cserny.generated.MediaMoveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/media-moves",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MediaMoveController implements ApiApi {

    @Autowired
    MediaMoveService service;

    @PostMapping
    @Override
    public ResponseEntity<List<MediaMoveError>> moveMedia(@RequestBody @Valid MediaMoveRequest moveRequest) {
        return ResponseEntity.ok(service.moveMedia(moveRequest.getFileGroup(), moveRequest.getType()));
    }

    @PostMapping("/all")
    @Override
    public ResponseEntity<List<MediaMoveError>> moveAllMedia(@RequestBody @Valid List<@Valid MediaMoveRequest> moveRequests) {
        List<MediaMoveError> errors = new ArrayList<>();
        for (MediaMoveRequest request : moveRequests) {
            errors.addAll(service.moveMedia(request.getFileGroup(), request.getType()));
        }
        return ResponseEntity.ok(errors);
    }
}
