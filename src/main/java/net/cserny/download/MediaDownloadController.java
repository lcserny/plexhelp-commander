package net.cserny.download;

import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.DownloadedMediaData;
import net.cserny.generated.MediaDownloadResourceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/media-downloads",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class MediaDownloadController implements MediaDownloadResourceApi {

    @Autowired
    MediaDownloadService service;

    @GetMapping
    @Override
    public ResponseEntity<List<DownloadedMediaData>> downloadsCompleted(@RequestParam Integer year,
                                                                        @RequestParam Integer month,
                                                                        @RequestParam Integer day) {
        return ResponseEntity.ok(service.retrieveAllFromDate(LocalDate.of(year, month, day)));
    }
}
