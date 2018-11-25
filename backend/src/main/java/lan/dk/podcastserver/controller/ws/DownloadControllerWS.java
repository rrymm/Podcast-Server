package lan.dk.podcastserver.controller.ws;

import com.github.davinkevin.podcastserver.entity.Item;
import com.github.davinkevin.podcastserver.manager.ItemDownloadManager;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * Created by kevin on 27/06/2014.
 */
@Controller
public class DownloadControllerWS {

    private final ItemDownloadManager IDM;

    public DownloadControllerWS(ItemDownloadManager IDM) {
        this.IDM = IDM;
    }

    @MessageMapping("/download/stop")
    public void stop(Item item) {
        IDM.stopDownload(item.getId());
    }

    @MessageMapping("/download/toogle")
    public void toogle(Item item) {
        IDM.toggleDownload(item.getId());
    }
    
}
