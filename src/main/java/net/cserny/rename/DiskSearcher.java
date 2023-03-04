package net.cserny.rename;

import net.cserny.filesystem.FilesystemConfig;
import net.cserny.filesystem.LocalFileService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DiskSearcher {

    @Inject
    FilesystemConfig filesystemConfig;

    @Inject
    LocalFileService localFileService;


}
