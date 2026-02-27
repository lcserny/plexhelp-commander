package net.cserny.task.subs;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cserny.support.BaseDocument;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "subs_reduced_media")
public class SubtitleReducedMedia extends BaseDocument {

    @Id
    private ObjectId id;

    @Field("filePath")
    @Indexed(name = "subtitleReducedMedia_filePath_idx")
    private String filePath;

    @Field("reduced")
    private boolean reduced;
}
