package net.cserny.monitoring;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Data
@Document("monitored_data")
public class MonitoredData {

    @Id
    private ObjectId id;
    private Instant timestamp;
    private String message;
}
