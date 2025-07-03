package net.cserny.move;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Document(collection = "automove_media")
public class AutoMoveMedia {

    @Id
    private ObjectId id;
    @Field("file_name")
    private String fileName;
    @Field("moved_name")
    private String movedName;
    @Field("move_date")
    private Instant moveDate;
    @Field("similarity_percent")
    private int similarityPercent;
    @Field("origin")
    private String origin;
    @Field("media_type")
    private String type;
}
