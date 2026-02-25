package net.cserny.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.core.command.LocalCommandService;
import net.cserny.generated.ApiApi;
import net.cserny.generated.CommandRequest;
import net.cserny.generated.CommandResponse;
import net.cserny.generated.Status;
import net.cserny.support.CommanderController;
import org.springframework.http.HttpStatus;
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

        CommandResponse response = localCommandService.execute(commandRequest.getName(), params)
                .map(result -> CommandResponse.builder()
                        .status(result.isSuccess() ? Status.SUCCESS : Status.FAILED)
                        .build())
                .orElseGet(() -> new CommandResponse().status(Status.NOT_FOUND));

        return switch (response.getStatus()) {
            case SUCCESS, NOT_FOUND -> ResponseEntity.ok(response);
            case FAILED -> new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }
}
