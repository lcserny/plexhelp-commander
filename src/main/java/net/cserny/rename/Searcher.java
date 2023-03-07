package net.cserny.rename;

public interface Searcher {

    RenamedMediaOptions search(NameNormalizer.NameYear nameYear, MediaFileType type);
}
