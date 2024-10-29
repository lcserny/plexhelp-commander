package net.cserny.download;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "download_cache")
public class DownloadedMedia {

    @Id
    private ObjectId id;
    @Field("file_name")
    private String fileName;
    @Field("file_size")
    private long fileSize;
    @Field("date_downloaded")
    @Indexed(name = "dateDownloaded_idx")
    private Instant dateDownloaded;
    @Field("tried_automove")
    @Indexed(name = "triedAutoMove_idx")
    private boolean triedAutoMove;
}
