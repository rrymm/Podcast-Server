package lan.dk.podcastserver.service;

import com.sapher.youtubedl.*;
import io.vavr.control.Option;
import lan.dk.podcastserver.service.properties.ExternalTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

import static io.vavr.API.Try;

/**
 * Created by kevin on 13/05/2018
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeDlService {
    private final ExternalTools externalTools;

    public Option<YoutubeDLResponse> extractName(String url) {
        YoutubeDLRequest request = new YoutubeDLRequest(url, null);
        request.setOption("get-filename");

        return Try(() -> YoutubeDL.execute(request))
                .toOption();
    }

    public YoutubeDLResponse download(String url, Path destination, DownloadProgressCallback callback) throws YoutubeDLException {
        String name = destination.getFileName().toString();
        String downloadLocation = destination.getParent().toAbsolutePath().toString();

        YoutubeDLRequest r = new YoutubeDLRequest(url, downloadLocation);
        r.setOption("retries", 10);
        r.setOption("output", name);

        return YoutubeDL.execute(r, (progress, etaInSeconds) -> log.info("p: {}, s:{}", progress, etaInSeconds));
    }

    @PostConstruct
    public void postContruct() {
        YoutubeDL.setExecutablePath(externalTools.getYoutubedl());
    }
}
