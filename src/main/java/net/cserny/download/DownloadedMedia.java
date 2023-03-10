package net.cserny.download;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@MongoEntity(collection = "download_cache")
public class DownloadedMedia {

    public ObjectId id;
    @BsonProperty("file_name")
    public String fileName;
    @BsonProperty("file_size")
    public long fileSize;
    @BsonProperty("date_downloaded")
    public LocalDateTime dateDownloaded;
}
