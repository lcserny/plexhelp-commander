package net.cserny.magnet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cserny.support.BaseDocument;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Document("magnet")
public class Magnet extends BaseDocument {

    @Id
    private ObjectId id;
    @Indexed(name = "name_idx")
    private String name;
    @Indexed(name = "hash_idx")
    private String hash;
    private String url;
    private Instant dateAdded;
    private boolean downloaded;
    private Instant dateDownloaded;
}
