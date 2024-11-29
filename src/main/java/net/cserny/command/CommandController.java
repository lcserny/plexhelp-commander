package net.cserny.command;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.CommandRequest;
import net.cserny.generated.CommandResourceApi;
import net.cserny.generated.CommandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/commands",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CommandController implements CommandResourceApi {

    @Autowired
    LocalCommandService localCommandService;

    @PostMapping
    @Override
    public ResponseEntity<CommandResponse> executeCommand(@RequestBody @Validated CommandRequest commandRequest) {
        String[] params = null;
        if (commandRequest.getParams() != null) {
            params = commandRequest.getParams().toArray(new String[0]);
        }
        return ResponseEntity.ok(localCommandService.execute(commandRequest.getName(), params));
    }
}
