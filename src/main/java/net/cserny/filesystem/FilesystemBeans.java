package net.cserny.filesystem;

import io.quarkus.arc.DefaultBean;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@Singleton
public class FilesystemBeans {

    @Produces
    @DefaultBean
    public FileSystem fileSystem() {
        return FileSystems.getDefault();
    }
}
