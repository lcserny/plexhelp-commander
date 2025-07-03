package net.cserny;

import net.cserny.download.DownloadedMedia;
import net.cserny.generated.DownloadedMediaData;
import net.cserny.generated.MagnetData;
import net.cserny.magnet.Magnet;
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

    DownloadedMediaData downloadedMediaToDownloadedMediaData(DownloadedMedia media);

    default OffsetDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    default String map(ObjectId objectId) {
        if (objectId == null) {
            return null;
        }
        return objectId.toString();
    }
}
