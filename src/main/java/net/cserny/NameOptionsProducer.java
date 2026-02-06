package net.cserny;

import net.cserny.generated.MediaFileType;
import net.cserny.generated.RenamedMediaOptions;

public interface NameOptionsProducer {

    RenamedMediaOptions produceNames(String name, MediaFileType type);
}
