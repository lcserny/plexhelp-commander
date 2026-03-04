package net.cserny.api;

import net.cserny.api.NameNormalizer.NameYear;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.RenamedMediaOptions;

public interface RenameSearcher {

    RenamedMediaOptions search(NameYear nameYear, MediaFileType type);
}
