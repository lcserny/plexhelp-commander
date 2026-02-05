package net.cserny.core.rename;

import net.cserny.generated.MediaFileType;
import net.cserny.generated.RenamedMediaOptions;

public interface Searcher {

    RenamedMediaOptions search(NameNormalizer.NameYear nameYear, MediaFileType type);
}
