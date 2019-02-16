package com.github.davinkevin.podcastserver.controller.api

import com.github.davinkevin.podcastserver.item.ItemService
import com.github.davinkevin.podcastserver.service.UrlService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.function.server.ServerResponse.seeOther
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.*

//@Controller
@RequestMapping("/api/podcasts/{idPodcast}/items")
class ItemRedirection(private val itemService: ItemService) {

    private var log = LoggerFactory.getLogger(ItemRedirection::class.java)

    @GetMapping(value = ["{id}/{file}"])
    fun file(@PathVariable id: UUID, exchange: ServerWebExchange) =
            itemService.findById(id)
                    .map {
                        if (it.isDownloaded()) {
                            UriComponentsBuilder.fromUri(UrlService.getDomainFromRequest(exchange))
                                    .pathSegment("data", it.podcast.title, it.fileName)
                                    .build().toUri()
                        } else {
                            URI(it.url)
                        }
                    }
                    .doOnNext { log.info("Redirect to {}", it)}
                    .flatMap { seeOther(it).build() }
}