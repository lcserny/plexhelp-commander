package net.cserny.filesystem;

import lombok.extern.slf4j.Slf4j;
import net.cserny.IntegrationTestWithRealFS;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Slf4j
@Disabled("Improved AutomoveService to group media so it should get move requests in multiple threads for the same TV show (episodes in same dir)")
//@EnabledIf("inWslWithMountedHostDrive")
@ExtendWith(MockitoExtension.class)
public class WSLLocalFileServiceTest extends IntegrationTestWithRealFS {

    private static final Path wslHostPublicDir = Paths.get("/mnt/c/Users/Public");

    static boolean inWslWithMountedHostDrive() {
        return Files.exists(wslHostPublicDir);
    }

    @Autowired
    private ExecutorService executorService;

    @Test
    @DisplayName("Executing multiple moves to the same destination at the same time should only succeed once")
    public void concurrentMoveWorksAsExpected() throws Exception {
        Path workDir = wslHostPublicDir.resolve("tmp").resolve(this.getClass().getSimpleName());

        LocalPath srcFile = null;
        LocalPath destFile = null;

        try {
            srcFile = createFile(1000, workDir.resolve("srcFile").toString());
            destFile = fileService.toLocalPath(workDir.resolve("destFile"));

            final LocalPath finalSrcFile = srcFile;
            final LocalPath finalDestFile = destFile;
            var callables = IntStream.range(0, 26).mapToObj(i -> (Callable<Boolean>) () -> {
                log.info("Calling thread #{}", i);
                boolean moved = fileService.move(finalSrcFile, finalDestFile);
                log.info("On thread #{}: destination file moved successfully.", i);
                return moved;
            }).toList();

            List<Future<Boolean>> results = executorService.invokeAll(callables, 2, TimeUnit.SECONDS);

            int successfulMoves = 0;
            for (Future<Boolean> result : results) {
                try {
                    if (result.get()) {
                        successfulMoves++;
                    }
                } catch (ExecutionException e) {
                    Throwable innerCause = e.getCause();
                    if (!(innerCause instanceof FileAlreadyExistsException)) {
                        throw e;
                    }
                }
            }

            if (successfulMoves != 1) {
                throw new RuntimeException("File was not moved or moved more than one time, race condition detected!");
            }
        } finally {
            cleanup(Arrays.asList(srcFile, destFile));
        }
    }

    private void cleanup(List<LocalPath> paths) throws IOException {
        for (LocalPath path : paths) {
            if (path != null && fileService.exists(path)) {
                fileService.delete(path);
            }
        }
    }
}
