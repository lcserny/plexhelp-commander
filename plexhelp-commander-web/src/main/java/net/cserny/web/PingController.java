package net.cserny.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.support.CommanderController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@CommanderController("/ping")
public class PingController implements ApiApi {

    @GetMapping
    @Override
    public ResponseEntity<Void> pingServer() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
