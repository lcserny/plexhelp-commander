package net.cserny.rename;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

@MongoEntity(collection = "online_cache")
public class OnlineResult {

    public ObjectId id;
    public String searchName;
    public int searchYear;
    public String coverPath;
    public String title;
    public LocalDate date;
    public String description;
    public List<String> cast;
    public OnlineResultType type;

    public enum OnlineResultType {
        MOVIE,
        TV
    }
}
