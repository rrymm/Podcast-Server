package lan.dk.podcastserver.controller.task;

import com.github.davinkevin.podcastserver.entity.Item;
import com.github.davinkevin.podcastserver.manager.ItemDownloadManager;
import com.github.davinkevin.podcastserver.utils.form.MovingItemInQueueForm;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Created by kevin on 26/12/2013.
 */
@RestController
@RequestMapping("/api/task/downloadManager")
public class IDMController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(IDMController.class);
    private final ItemDownloadManager IDM;

    @java.beans.ConstructorProperties({"IDM"})
    public IDMController(ItemDownloadManager IDM) {
        this.IDM = IDM;
    }

    @GetMapping("/queue")
    public Queue<Item> getDownloadList () {
        return IDM.getWaitingQueue();
    }

    @GetMapping("/downloading")
    public Set<Item> getDownloadingList () {
        return IDM.getItemsInDownloadingQueue();
    }

    @GetMapping("/downloading/{id}")
    public Item getDownloadingList (@PathVariable UUID id) {
        return IDM.getItemInDownloadingQueue(id);
    }

    @GetMapping("/current")
    public int getNumberOfCurrentDownload () {
        return IDM.getNumberOfCurrentDownload();
    }

    @GetMapping(value="/limit")
    public int getLimitParallelDownload () {
        return IDM.getLimitParallelDownload();
    }

    @PostMapping("/limit")
    public void setLimitParallelDownload (@RequestBody int setLimitParallelDownload) {
        IDM.setLimitParallelDownload(setLimitParallelDownload);
    }

    @GetMapping("/launch")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void launchDownload() {
        IDM.launchDownload();
    }

    // Action on ALL download :
    @GetMapping("/stopAllDownload")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void stopAllCurrentDownload() {
        IDM.stopAllDownload();
    }

    @GetMapping(value="/pauseAllDownload")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void pauseAllCurrentDownload() {
        IDM.pauseAllDownload();
    }

    @GetMapping(value="/restartAllDownload")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void restartAllCurrentDownload() {
        IDM.restartAllDownload();
    }

    // Action on id identified download :
    @PostMapping("/stopDownload/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void stopCurrentDownload(@PathVariable("id") UUID id) {
        IDM.stopDownload(id);
    }

    @PostMapping("/pauseDownload")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void pauseCurrentDownload(@RequestBody UUID id) {
        IDM.pauseDownload(id);
    }

    @PostMapping("/restartDownload")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void restartCurrentDownload(@RequestBody UUID id) {
        IDM.restartDownload(id);
    }

    @PostMapping("/toogleDownload/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void toggleCurrentDownload(@PathVariable("id") UUID id) {
        IDM.toggleDownload(id);
    }

    @PostMapping("/queue/add")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void addItemToQueue(@RequestBody UUID id) {
        IDM.addItemToQueue(id);
    }

    @DeleteMapping("/queue/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeItemFromQueue(@PathVariable UUID id) {
        IDM.removeItemFromQueue(id, false);
    }

    @DeleteMapping("/queue/{id}/andstop")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeItemFromQueueAndStopped(@PathVariable UUID id) {
        IDM.removeItemFromQueue(id, true);
    }

    @DeleteMapping("/queue")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void emptyQueue() {
        IDM.clearWaitingQueue();
    }

    @PostMapping("/move")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void moveItemInQueue(@RequestBody MovingItemInQueueForm movingItemInQueueForm) {
        IDM.moveItemInQueue(movingItemInQueueForm.getId(), movingItemInQueueForm.getPosition());
    }
}
