package net.cserny.rename;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MediaRenameRequest;
import net.cserny.generated.RenamedMediaOptions;
import net.cserny.support.CommanderController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/media-renames")
public class MediaRenameController implements ApiApi {

    private final MediaRenameService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<RenamedMediaOptions> produceRenames(@RequestBody @Valid MediaRenameRequest request) {
        return ResponseEntity.ok(service.produceNames(request.getName(), request.getType()));
    }
}
