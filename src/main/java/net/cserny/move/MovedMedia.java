package net.cserny.move;

import lombok.Builder;
import lombok.Data;
import net.cserny.generated.MediaFileType;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Builder
@Data
@Document(collection = "moved_media")
public class MovedMedia {

    @Id
    private ObjectId id;

    @Field
    private String source;

    @Field
    private String destination;

    @Field
    private long sizeBytes;

    @Field
    private String mediaName;

    @Field
    private Instant date;

    @Field
    private Integer season;

    @Field
    private Integer episode;

    @Field
    private MediaFileType mediaType;
}
