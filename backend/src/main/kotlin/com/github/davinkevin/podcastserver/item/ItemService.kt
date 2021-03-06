package com.github.davinkevin.podcastserver.item

import com.github.davinkevin.podcastserver.entity.Status
import com.github.davinkevin.podcastserver.manager.ItemDownloadManager
import com.github.davinkevin.podcastserver.service.FileService
import com.github.davinkevin.podcastserver.service.properties.PodcastServerParameters
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.nio.file.Paths
import java.util.*
import com.github.davinkevin.podcastserver.item.ItemRepositoryV2 as ItemRepository

/**
 * Created by kevin on 2019-02-09
 */
@Component
class ItemService(
        private val repository: ItemRepository,
        private val idm: ItemDownloadManager,
        private val p: PodcastServerParameters,
        private val fileService: FileService
) {

    private val log = LoggerFactory.getLogger(ItemService::class.java)!!

    fun deleteOldEpisodes() = repository.
            findAllToDelete( p.limitDownloadDate().toOffsetDateTime() )
            .doOnSubscribe { log.info("Deletion of old items") }
            .delayUntil { fileService.deleteItem(it) }
            .collectList()
            .flatMap { repository.updateAsDeleted(it.map { v -> v.id }) }

    fun findById(id: UUID) = repository.findById(id)

    fun reset(id: UUID): Mono<Item> = deleteItemFiles(id)
            .then(repository.resetById(id))

    private fun deleteItemFiles(id: UUID): Mono<Void> = id.toMono()
            .filter { !idm.isInDownloadingQueueById(it) }
            .filterWhen { repository.hasToBeDeleted(it) }
            .flatMap { repository.findById(it) }
            .delayUntil { item -> item.toMono()
                    .filter { it.isDownloaded() }
                    .filter { !it.fileName.isNullOrEmpty() }
                    .map { DeleteItemInformation(it.id, it.fileName!!, it.podcast.title) }
                    .flatMap { fileService.deleteItem(it) }
            }
            .then()

    fun search(q: String?, tags: List<String>, statuses: List<Status>, page: ItemPageRequest): Mono<PageItem> =
            repository.search(q = q, tags = tags, statuses = statuses, page = page)
}
