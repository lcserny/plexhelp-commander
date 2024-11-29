package net.cserny.download;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.ApiApi;
import net.cserny.generated.DownloadedMediaData;
import net.cserny.generated.SearchDownloadedMedia;
import net.cserny.generated.SearchDownloadedMediaDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/media-downloads",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class MediaDownloadController implements ApiApi {

    @Autowired
    MediaDownloadService service;

    @PostMapping
    @Override
    public ResponseEntity<List<DownloadedMediaData>> searchDownloadedMedia(@RequestBody @Valid SearchDownloadedMedia searchDownloadedMedia) {
        LocalDate searchDate = null;
        SearchDownloadedMediaDate date = searchDownloadedMedia.getDate();
        if (date != null) {
            searchDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
        }

        return ResponseEntity.ok(this.service.retrieveAllFrom(searchDate, searchDownloadedMedia.getNames(), searchDownloadedMedia.getDownloaded()));
    }
}
