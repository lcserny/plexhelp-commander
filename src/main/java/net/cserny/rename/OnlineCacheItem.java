package net.cserny.rename;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "online_cache")
public class OnlineCacheItem {

    @Id
    private ObjectId id;
    private String searchName;
    private Integer searchYear;
    private String coverPath;
    private String title;
    private Instant date;
    private String description;
    private List<String> cast;
    private MediaFileType mediaType;
}
