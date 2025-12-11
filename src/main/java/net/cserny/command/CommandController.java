package net.cserny.command;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.CommandRequest;
import net.cserny.generated.CommandResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/commands",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CommandController implements ApiApi {

    private final LocalCommandService localCommandService;

    @PostMapping
    @Override
    public ResponseEntity<CommandResponse> executeCommand(@Valid @RequestBody CommandRequest commandRequest) {
        String[] params = null;
        if (commandRequest.getParams() != null) {
            params = commandRequest.getParams().toArray(new String[0]);
        }
        return ResponseEntity.ok(localCommandService.execute(commandRequest.getName(), params));
    }
}
