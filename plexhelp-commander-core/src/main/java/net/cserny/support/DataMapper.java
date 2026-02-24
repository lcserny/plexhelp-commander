package net.cserny.support;

import net.cserny.core.download.DownloadedMedia;
import net.cserny.core.magnet.Magnet;
import net.cserny.core.move.MovedMedia;
import net.cserny.generated.DownloadedMediaData;
import net.cserny.generated.MagnetData;
import net.cserny.generated.MovedMediaData;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper
public interface DataMapper {

    DataMapper INSTANCE = Mappers.getMapper(DataMapper.class);

    MagnetData magnetToMagnetData(Magnet magnet);

    MovedMediaData movedMediaToMovedMediaData(MovedMedia movedMedia);

    DownloadedMediaData downloadedMediaToDownloadedMediaData(DownloadedMedia media);

    DownloadedMedia downloadedMediaDataToDownloadedMedia(DownloadedMediaData media);

    default OffsetDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toInstant();
    }

    default String map(ObjectId objectId) {
        if (objectId == null) {
            return null;
        }
        return objectId.toString();
    }

    default ObjectId map(String objectId) {
        if (objectId == null) {
            return null;
        }
        return new ObjectId(objectId);
    }
}
