package net.cserny.rename;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "online_cache")
public class OnlineCacheItem {

    @Id
    private ObjectId id;
    @Indexed(name = "searchName_idx")
    private String searchName;
    @Indexed(name = "searchYear_idx")
    private Integer searchYear;
    private String coverPath;
    private String title;
    private Instant date;
    private String description;
    private List<String> cast;
    @Indexed(name = "mediaType_idx")
    private String mediaType;
}
