package net.cserny.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/commands",
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CommandController {

    @Autowired
    LocalCommandService localCommandService;

    @PostMapping
    public CommandResponse executeCommand(@RequestBody CommandRequest commandRequest) {
        return localCommandService.execute(commandRequest.name(), commandRequest.params());
    }
}
