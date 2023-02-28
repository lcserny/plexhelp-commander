package net.cserny.filesystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.quarkus.arc.DefaultBean;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

@Singleton
public class FilesystemTestBeans {

    @Produces
    public FileSystem fileSystem() {
        return Jimfs.newFileSystem(Configuration.unix());
    }
}
