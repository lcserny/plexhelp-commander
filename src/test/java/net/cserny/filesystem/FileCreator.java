package net.cserny.filesystem;

import java.io.IOException;
import java.nio.file.Files;

public class FileCreator {

    private FileCreator() {
    }

    public static LocalPath createFile(LocalFileService service, String somePath) throws IOException {
        LocalPath localPath = service.produceLocalPath(somePath);
        if (localPath.path().getParent() != null) {
            Files.createDirectories(localPath.path().getParent());
        }
        Files.createFile(localPath.path());
        return localPath;
    }

    public static LocalPath createFile(LocalFileService service, String somePath, int size) throws IOException {
        LocalPath localPath = createFile(service, somePath);
        Files.write(localPath.path(), new byte[size]);
        return localPath;
    }
}
