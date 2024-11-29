package net.cserny.magnet;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.MagnetData;
import net.cserny.generated.PaginatedMagnets;
import net.cserny.generated.PaginatedMagnetsPage;
import net.cserny.generated.TorrentMagnetResourceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static net.cserny.WebConfig.createPageable;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/magnets", produces = MediaType.APPLICATION_JSON_VALUE)
public class MagnetController implements TorrentMagnetResourceApi {

    private final MagnetService service;

    @Autowired
    public MagnetController(MagnetService service) {
        this.service = service;
    }

    @PostMapping
    @Override
    public ResponseEntity<MagnetData> addMagnet(@RequestBody @Validated String magnetLink) {
        return new ResponseEntity<>(this.service.addMagnet(magnetLink), HttpStatus.CREATED);
    }

    @GetMapping
    @Override
    public ResponseEntity<PaginatedMagnets> getAll(@RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "size", required = false) Integer size,
                                                   @RequestParam(value = "sort", required = false) List<String> sort,
                                                   @RequestParam(value = "name", required = false) String name) {
        Pageable pageable = createPageable(page, size, sort);
        Page<MagnetData> resultPage = this.service.getAll(pageable, name);
        PaginatedMagnetsPage magnetsPage = new PaginatedMagnetsPage(resultPage.getSize(), resultPage.getNumber(), resultPage.getTotalElements(), resultPage.getTotalPages());
        return ResponseEntity.ok(new PaginatedMagnets(resultPage.getContent(), magnetsPage));
    }
}
