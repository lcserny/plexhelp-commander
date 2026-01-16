package net.cserny.rename;

import lombok.*;
import net.cserny.generated.MediaFileType;
import net.cserny.support.BaseDocument;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"id"}, callSuper = true)
@CompoundIndexes({
        @CompoundIndex(name = "nameType_idx", def = "{'searchName': 1, 'mediaType': 1}"),
        @CompoundIndex(name = "nameYearType_idx", def = "{'searchName': 1, 'searchYear': 1, 'mediaType': 1}")
})
@Document(collection = "online_cache")
public class OnlineCacheItem extends BaseDocument {

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
