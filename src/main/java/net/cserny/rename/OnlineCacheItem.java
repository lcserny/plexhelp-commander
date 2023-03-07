package net.cserny.rename;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@MongoEntity(collection = "online_cache")
public class OnlineCacheItem {

    public ObjectId id;
    public String searchName;
    public Integer searchYear;
    public String coverPath;
    public String title;
    public LocalDate date;
    public String description;
    public List<String> cast;
    public MediaFileType mediaType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlineCacheItem item = (OnlineCacheItem) o;
        return Objects.equals(id, item.id) && Objects.equals(searchName, item.searchName) && Objects.equals(searchYear, item.searchYear) && Objects.equals(coverPath, item.coverPath) && Objects.equals(title, item.title) && Objects.equals(date, item.date) && Objects.equals(description, item.description) && Objects.equals(cast, item.cast) && mediaType == item.mediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, searchName, searchYear, coverPath, title, date, description, cast, mediaType);
    }

    @Override
    public String toString() {
        return "OnlineCacheItem{" +
                "id=" + id +
                ", searchName='" + searchName + '\'' +
                ", searchYear=" + searchYear +
                ", coverPath='" + coverPath + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", cast=" + cast +
                ", mediaType=" + mediaType +
                '}';
    }
}
