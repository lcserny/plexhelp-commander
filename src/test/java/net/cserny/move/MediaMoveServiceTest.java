package net.cserny.move;

import io.quarkus.test.junit.QuarkusTest;
import net.cserny.AbstractInMemoryFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MediaMoveServiceTest extends AbstractInMemoryFileService {

    @Inject
    MediaMoveService service;

    @Test
    @DisplayName("It works, now adjust me")
    public void itWorks() {

    }
}