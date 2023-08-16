package net.cserny.move;

import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/media-moves",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MediaMoveController {

    @Autowired
    MediaMoveService service;

    @PostMapping
    public List<MediaMoveError> moveMedia(@RequestBody MediaMoveRequest moveRequest) {
        log.info("Received moveMedia request with payload {}", moveRequest);
        return service.moveMedia(moveRequest.fileGroup(), moveRequest.type());
    }
}
