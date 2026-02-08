package net.cserny.core.move;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cserny.generated.MediaDescriptionData;
import net.cserny.generated.MediaFileType;
import net.cserny.support.BaseDocument;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@Document(collection = "moved_media")
public class MovedMedia extends BaseDocument {

    @Id
    private ObjectId id;

    @Field
    private String source;

    @Field
    private String destination;

    @Field
    private Long sizeBytes;

    @Field
    @Indexed(name = "mediaName_idx")
    private String mediaName;

    @Field
    private Instant date;

    @Field
    private Integer season;

    @Field
    private Integer episode;

    @Field
    private MediaFileType mediaType;

    @Field
    private Boolean deleted;

    @Field
    private MediaDescriptionData mediaDesc;
}
