package net.cserny.command;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.CommandRequest;
import net.cserny.generated.CommandResponse;
import net.cserny.support.CommanderController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/commands")
public class CommandController implements ApiApi {

    private final LocalCommandService localCommandService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<CommandResponse> executeCommand(@Valid @RequestBody CommandRequest commandRequest) {
        String[] params = null;
        if (commandRequest.getParams() != null) {
            params = commandRequest.getParams().toArray(new String[0]);
        }
        return ResponseEntity.ok(localCommandService.execute(commandRequest.getName(), params));
    }
}
