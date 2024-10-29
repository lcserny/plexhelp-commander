package net.cserny;

import net.cserny.generated.MagnetData;
import net.cserny.magnet.Magnet;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface DataMapper {

    DataMapper INSTANCE = Mappers.getMapper(DataMapper.class);

    MagnetData magnetToMagnetData(Magnet magnet);

    default OffsetDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
