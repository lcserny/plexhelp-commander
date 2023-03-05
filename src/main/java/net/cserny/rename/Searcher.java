package net.cserny.rename;

import java.util.List;

public interface Searcher {

    List<String> search(NameNormalizer.NameYear nameYear, MediaFileType type);
}
