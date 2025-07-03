package net.cserny.download;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.cserny.generated.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        LocalDate searchDate = this.convertToLocalDate(searchDownloadedMedia.getDate());
        return ResponseEntity.ok(this.service.retrieveAllFrom(searchDate, searchDownloadedMedia.getNames(), searchDownloadedMedia.getDownloaded()));
    }

    @PostMapping("/paginated")
    @Override
    public ResponseEntity<PaginatedDownloads> searchDownloadedMediaPaginated(@RequestBody @Valid SearchDownloadedMedia searchDownloadedMedia, Pageable pageable) {
        LocalDate searchDate = this.convertToLocalDate(searchDownloadedMedia.getDate());
        Page<DownloadedMediaData> resultPage = this.service.retrieveAllPaginatedFrom(searchDate, searchDownloadedMedia.getNames(), searchDownloadedMedia.getDownloaded(), pageable);
        PaginatedBase1Page downloadsPage = new PaginatedBase1Page(resultPage.getSize(), resultPage.getNumber(), resultPage.getTotalElements(), resultPage.getTotalPages());
        return ResponseEntity.ok(new PaginatedDownloads(downloadsPage, resultPage.getContent()));
    }

    private LocalDate convertToLocalDate(SearchDownloadedMediaDate searchDate) {
        if (searchDate != null) {
            return LocalDate.of(searchDate.getYear(), searchDate.getMonth(), searchDate.getDay());
        }
        return null;
    }
}
