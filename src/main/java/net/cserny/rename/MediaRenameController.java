package net.cserny.rename;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MediaRenameRequest;
import net.cserny.generated.RenamedMediaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/media-renames",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MediaRenameController implements ApiApi {

    @Autowired
    MediaRenameService service;

    @PostMapping
    @Override
    public ResponseEntity<RenamedMediaOptions> produceRenames(@RequestBody @Valid MediaRenameRequest request) {
        return ResponseEntity.ok(service.produceNames(request.getName(), request.getType()));
    }
}
