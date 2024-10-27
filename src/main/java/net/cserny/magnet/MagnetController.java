package net.cserny.magnet;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.MagnetData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/magnets", produces = MediaType.APPLICATION_JSON_VALUE)
public class MagnetController {

    private final MagnetService service;

    @Autowired
    public MagnetController(MagnetService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MagnetData addMagnet(@RequestBody String magnetLink) {
        log.info("Received addMagnet request with link: {}", magnetLink);
        return this.service.addMagnet(magnetLink);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<MagnetData> getAll(Pageable pageable) {
        log.info("Received getAll request for page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());
        return new PagedModel<>(this.service.getAll(pageable));
    }
}
