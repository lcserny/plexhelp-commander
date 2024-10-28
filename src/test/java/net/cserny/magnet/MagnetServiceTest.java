package net.cserny.magnet;

import net.cserny.generated.MagnetData;
import net.cserny.qtorrent.TorrentRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MagnetServiceTest {

    private MagnetRepository repositoryMock;
    private TorrentRestClient clientMock;

    private MagnetService service;

    @BeforeEach
    public void setup() {
        this.repositoryMock = mock(MagnetRepository.class);
        this.clientMock = mock(TorrentRestClient.class);
        this.service = new MagnetService(this.repositoryMock, this.clientMock);
    }

    @Test
    @DisplayName("Retrieving all magnets returns them paginated")
    public void getAll() {
        List<Magnet> magnets = new ArrayList<>();
        magnets.add(new Magnet());
        Page<Magnet> magnetsPage = new PageImpl<>(magnets);

        when(this.repositoryMock.findAll(any(Pageable.class))).thenReturn(magnetsPage);

        Page<MagnetData> returnedPage = this.service.getAll(mock(Pageable.class), nullable(String.class));

        assertEquals(1, returnedPage.getTotalPages());
        assertEquals(1, returnedPage.getTotalElements());
    }

    @Test
    @DisplayName("Adding magnet link returns magnet data created")
    public void addMagnet() {
        var hash = "abc";
        var name = "myName";
        var magnetLink = format("magnet:?xt=urn:btih:%s&dn=%s&tr=whatever", hash, name);

        var magnet = new Magnet();
        magnet.setName(name);
        magnet.setHash(hash);

        when(repositoryMock.save(any(Magnet.class))).thenReturn(magnet);

        MagnetData data = this.service.addMagnet(magnetLink);

        assertEquals(hash, data.getHash());
        assertEquals(name, data.getName());
    }
}