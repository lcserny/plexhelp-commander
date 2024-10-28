package net.cserny.magnet;

import lombok.extern.slf4j.Slf4j;
import net.cserny.DataMapper;
import net.cserny.generated.MagnetData;
import net.cserny.qtorrent.TorrentRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;

@Slf4j
@Service
public class MagnetService {

    private static final String NAME_KEY = "dn";
    private static final String HASH_KEY = "xt";

    private final MagnetRepository repository;
    private final TorrentRestClient restClient;

    @Autowired
    public MagnetService(MagnetRepository repository, TorrentRestClient restClient) {
        this.repository = repository;
        this.restClient = restClient;
    }

    public MagnetData addMagnet(String magnetLink) {
        validateMagnetLink(magnetLink);

        String sid = this.restClient.generateSid();
        this.restClient.addMagnet(sid, magnetLink);

        log.info("Added magnet to torrent client");

        var dataMap = new HashMap<String, String>();
        var stripped = magnetLink.substring(8);
        var pairs = stripped.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                dataMap.put(key, value);
            }
        }

        var name = dataMap.get(NAME_KEY);
        var hashPayload = dataMap.get(HASH_KEY);
        var hash = hashPayload.substring(hashPayload.lastIndexOf(":") + 1).toLowerCase();

        var magnet = new Magnet();
        magnet.setUrl(magnetLink);
        magnet.setName(name);
        magnet.setHash(hash);
        magnet.setDateAdded(Instant.now(Clock.systemUTC()));
        var saved = this.repository.save(magnet);

        log.info("Saved magnet in database");

        return DataMapper.INSTANCE.magnetToMagnetData(saved);
    }

    private void validateMagnetLink(String magnetLink) {
        if (magnetLink == null || !magnetLink.startsWith("magnet:?")) {
            throw new IllegalArgumentException("Magnet link provided is not valid");
        }

        if (!magnetLink.contains("xt=") || !magnetLink.contains("dn=")) {
            throw new IllegalArgumentException("Magnet link provided does not contain needed fields");
        }
    }

    public Page<MagnetData> getAll(Pageable pageable) {
        Page<Magnet> all = this.repository.findAll(pageable);
        log.info("Retrieved paginated magnets from the database");
        return all.map(DataMapper.INSTANCE::magnetToMagnetData);
    }
}
