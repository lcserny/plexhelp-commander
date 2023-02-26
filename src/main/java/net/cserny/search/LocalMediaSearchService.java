package net.cserny.search;

import net.cserny.filesystem.LocalFileService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LocalMediaSearchService {

    @Inject
    LocalFileService fileService;


}
