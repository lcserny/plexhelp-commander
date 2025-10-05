package net.cserny.monitoring;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MonitoringRepository extends MongoRepository<MonitoredData, ObjectId> {
}
