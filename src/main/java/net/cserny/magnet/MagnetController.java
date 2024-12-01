package net.cserny.magnet;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.MagnetData;
import net.cserny.generated.PaginatedBase1Page;
import net.cserny.generated.PaginatedMagnets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/magnets", produces = MediaType.APPLICATION_JSON_VALUE)
public class MagnetController implements ApiApi {

    private final MagnetService service;

    @Autowired
    public MagnetController(MagnetService service) {
        this.service = service;
    }

    @PostMapping
    @Override
    public ResponseEntity<MagnetData> addMagnet(@RequestBody @Valid String magnetLink) {
        return new ResponseEntity<>(this.service.addMagnet(magnetLink), HttpStatus.CREATED);
    }

    @GetMapping
    @Override
    public ResponseEntity<PaginatedMagnets> getAll(@Valid @RequestParam(value = "name", required = false) String name, Pageable page) {
        Page<MagnetData> resultPage = this.service.getAll(page, name);
        PaginatedBase1Page magnetsPage = new PaginatedBase1Page(resultPage.getSize(), resultPage.getNumber(), resultPage.getTotalElements(), resultPage.getTotalPages());
        return ResponseEntity.ok(new PaginatedMagnets(magnetsPage, resultPage.getContent()));
    }
}
