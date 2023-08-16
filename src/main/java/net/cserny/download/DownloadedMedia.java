package net.cserny.download;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "download_cache")
public class DownloadedMedia {

    @Id
    public ObjectId id;
    @BsonProperty("file_name")
    public String fileName;
    @BsonProperty("file_size")
    public long fileSize;
    @BsonProperty("date_downloaded")
    public LocalDateTime dateDownloaded;
}
