package net.cserny.core.rename;

import net.cserny.api.NameNormalizer.NameYear;
import net.cserny.generated.MediaFileType;
import net.cserny.generated.RenamedMediaOptions;

public interface Searcher {

    RenamedMediaOptions search(NameYear nameYear, MediaFileType type);
}
