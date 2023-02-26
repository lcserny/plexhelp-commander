package net.cserny.search;

import net.cserny.filesystem.LocalFileService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LocalMediaSearchService {

    @Inject
    LocalFileService fileService;

    // TODO: use fileService to search path and get files (pass max depth param)
    //  Filter files returned for configured exclude paths
    //  Check files if video by content type and configured file size
    //  Maybe sort the files before returning?
}
