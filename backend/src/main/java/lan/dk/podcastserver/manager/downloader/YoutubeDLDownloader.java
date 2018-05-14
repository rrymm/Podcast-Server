package lan.dk.podcastserver.manager.downloader;

import com.sapher.youtubedl.YoutubeDLResponse;
import io.vavr.collection.Stream;
import io.vavr.control.Try;
import lan.dk.podcastserver.entity.Item;
import lan.dk.podcastserver.repository.ItemRepository;
import lan.dk.podcastserver.repository.PodcastRepository;
import lan.dk.podcastserver.service.MimeTypeService;
import lan.dk.podcastserver.service.YoutubeDlService;
import lan.dk.podcastserver.service.properties.PodcastServerParameters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.vavr.API.Try;

/**
 * Created by kevin on 13/05/2018
 */
@Slf4j
@Component("YoutubeDLDownloader")
@Scope("prototype")
public class YoutubeDLDownloader extends AbstractDownloader {

    private final YoutubeDlService youtubeService;

    public YoutubeDLDownloader(ItemRepository itemRepository, PodcastRepository podcastRepository, PodcastServerParameters podcastServerParameters, SimpMessagingTemplate template, MimeTypeService mimeTypeService, YoutubeDlService youtubeService) {
        super(itemRepository, podcastRepository, podcastServerParameters, template, mimeTypeService);
        this.youtubeService = youtubeService;
    }

    @Override
    public Item download() {
        String url = downloadingItem.getUrls().head();

        target = getTargetFile(item);

        YoutubeDLResponse r = Try(() -> youtubeService.download(url, target, null)).getOrElseThrow(e -> new RuntimeException(e));

        finishDownload();

        return item;
    }

    @Override
    public String getFileName(Item item) {
        return downloadingItem
                .url()
                .flatMap(youtubeService::extractName)
                .map(YoutubeDLResponse::getOut)
                .map(s -> s.replaceAll("\n", "").replaceAll("[^a-zA-Z0-9.-]", "_"))
                .getOrElseThrow(() -> new RuntimeException("Error during creation of filename of " + item.getUrl()));
    }

    @Override
    public void finishDownload() {

        Path parent = target.getParent();

        Path savedPath = Try(() -> Stream.ofAll(Files.walk(parent))).getOrElseThrow(e -> new RuntimeException(e))
                .find(v -> v.toAbsolutePath().toString().startsWith(target.toAbsolutePath().toString()))
                .peek(v -> log.info("File found: {}", v.toAbsolutePath().toString()))
                .getOrElseThrow(() -> new RuntimeException("No file found after download..."));

        String extension = FilenameUtils.getExtension(savedPath.getFileName().toString());
        String fileNameWithoutAnyExtension = FilenameUtils.removeExtension(FilenameUtils.removeExtension(target.getFileName().toString()));

        target = target.resolveSibling(fileNameWithoutAnyExtension + "." + extension + temporaryExtension);

        Try.run(() -> Files.move(savedPath, target));

        super.finishDownload();
    }

    @Override
    public Integer compatibility(DownloadingItem ditem) {
        return ditem.getUrls().length() == 1 && ditem.getUrls().head().contains("www.youtube.com")
                ? 1
                : Integer.MAX_VALUE;
    }
}
