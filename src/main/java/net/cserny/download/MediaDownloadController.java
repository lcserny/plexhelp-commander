package net.cserny.download;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
public class MediaDownloadController {

    @Autowired
    MediaDownloadService service;

    @GetMapping
    public List<DownloadedMedia> downloadsCompleted(@RequestParam int year, @RequestParam int month, @RequestParam int day) {
        return service.retrieveAllFromDate(LocalDate.of(year, month, day));
    }
}
