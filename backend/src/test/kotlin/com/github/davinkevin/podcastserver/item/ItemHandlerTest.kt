package com.github.davinkevin.podcastserver.item

import com.github.davinkevin.podcastserver.entity.Status
import com.github.davinkevin.podcastserver.extension.json.assertThatJson
import com.github.davinkevin.podcastserver.service.FileService
import com.github.davinkevin.podcastserver.service.properties.PodcastServerParameters
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.nio.file.Path
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import java.util.*

/**
 * Created by kevin on 2019-02-12
 */
@WebFluxTest(controllers = [ItemHandler::class])
@Import(ItemRoutingConfig::class)
@ImportAutoConfiguration(ErrorWebFluxAutoConfiguration::class)
class ItemHandlerTest {

    @Autowired lateinit var rest: WebTestClient
    @MockBean lateinit var itemService: ItemService
    @MockBean lateinit var p: PodcastServerParameters
    @MockBean lateinit var fileService: FileService

    val item = Item(
            id = UUID.fromString("27184b1a-7642-4ffd-ac7e-14fb36f7f15c"),
            title = "Foo",
            url = "https://external.domain.tld/foo/bar.mp4",

            pubDate = now(),
            downloadDate = now(),
            creationDate = now(),

            description = "desc",
            mimeType = null,
            length = 100,
            fileName = null,
            status = Status.NOT_DOWNLOADED,

            podcast = PodcastForItem(
                    id = UUID.fromString("8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9"),
                    title = "Podcast Bar",
                    url = "https://external.domain.tld/bar.rss"
            ),
            cover = CoverForItem(
                    id = UUID.fromString("f4efe8db-7abf-4998-b15c-9fa2e06096a1"),
                    url = "https://external.domain.tld/foo/bar.png",
                    width = 200,
                    height = 200
            )
    )

    @Nested
    @DisplayName("should delete")
    inner class ShouldDelete {

        @Test
        fun `and returns ok`() {
            /* Given */
            whenever(itemService.deleteOldEpisodes()).thenReturn(Mono.just(1).then())

            /* When */
            rest.delete()
                    .uri("/api/v1/items/clean")
                    .exchange()
                    /* Then */
                    .expectStatus().isOk
        }
    }

    @Nested
    @DisplayName("should serve file")
    inner class ShouldServeFile {

        @Test
        fun `by redirecting to local file server`() {
            /* Given */
            val itemDownloaded = item.copy(
                    status = Status.FINISH, fileName = "file_to_download.mp4"
            )
            whenever(itemService.findById(item.id)).thenReturn(itemDownloaded.toMono())

            /* When */
            rest
                    .get()
                    .uri("https://localhost:8080/api/v1/podcasts/{idPodcast}/items/{id}/{file}", item.podcast.id, item.id, "download.mp4")
                    .exchange()
                    /* Then */
                    .expectStatus().isSeeOther
                    .expectHeader()
                    .valueEquals("Location", "https://localhost:8080/data/Podcast%20Bar/file_to_download.mp4")
        }

        @Test
        fun `by redirecting if element is not downloaded`() {
            /* Given */
            whenever(itemService.findById(item.id)).thenReturn(item.toMono())
            /* When */
            rest
                    .get()
                    .uri("/api/v1/podcasts/{idPodcast}/items/{id}/{file}", item.podcast.id, item.id, "download.mp4")
                    .exchange()
                    /* Then */
                    .expectStatus().isSeeOther
                    .expectHeader()
                    .valueEquals("Location", "https://external.domain.tld/foo/bar.mp4")

        }
    }

    @Nested
    @DisplayName("should serve cover")
    inner class ShouldServerCover {

        @Test
        fun `by redirecting to local file server if cover exists locally`() {
            /* Given */
            whenever(itemService.findById(item.id)).thenReturn(item.toMono())
            whenever(fileService.exists(any())).then { it.getArgument<Path>(0).toMono() }

            /* When */
            rest
                    .get()
                    .uri("https://localhost:8080/api/v1/podcasts/{idPodcast}/items/{id}/cover.{ext}", item.podcast.id, item.id, "png")
                    .exchange()
                    /* Then */
                    .expectStatus().isSeeOther
                    .expectHeader()
                    .valueEquals("Location", "https://localhost:8080/data/Podcast%20Bar/27184b1a-7642-4ffd-ac7e-14fb36f7f15c.png")
        }

        @Test
        fun `by redirecting to external file if cover does not exist locally`() {
            /* Given */
            whenever(itemService.findById(item.id)).thenReturn(item.toMono())
            whenever(fileService.exists(any())).then { Mono.empty<Path>() }

            /* When */
            rest
                    .get()
                    .uri("https://localhost:8080/api/v1/podcasts/{idPodcast}/items/{id}/cover.{ext}", item.podcast.id, item.id, "png")
                    .exchange()
                    /* Then */
                    .expectStatus().isSeeOther
                    .expectHeader()
                    .valueEquals("Location", "https://external.domain.tld/foo/bar.png")
        }
    }

    @Nested
    @DisplayName("should find by id")
    inner class ShouldFindById {

        private val notDownloadedItem = Item(
                id = UUID.fromString("27184b1a-7642-4ffd-ac7e-14fb36f7f15c"),
                title = "Foo",
                url = "https://external.domain.tld/foo/bar.mp4",

                pubDate = OffsetDateTime.parse("2019-02-01T13:14:15.000Z"),
                downloadDate = null,
                creationDate = OffsetDateTime.parse("2019-02-05T13:14:15.000Z"),

                description = "desc",
                mimeType = null,
                length = 100,
                fileName = null,
                status = Status.NOT_DOWNLOADED,

                podcast = PodcastForItem(
                        id = UUID.fromString("8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9"),
                        title = "Podcast Bar",
                        url = "https://external.domain.tld/bar.rss"
                ),
                cover = CoverForItem(
                        id = UUID.fromString("f4efe8db-7abf-4998-b15c-9fa2e06096a1"),
                        url = "https://external.domain.tld/foo/bar.png",
                        width = 200,
                        height = 200
                )
        )

        @Test
        fun `with not downloaded item`() {
            /* Given */
            val pid = notDownloadedItem.podcast.id
            val iid = notDownloadedItem.id
            whenever(itemService.findById(iid)).thenReturn(notDownloadedItem.toMono())
            /* When */
            rest.get()
                    .uri("/api/v1/podcasts/{pid}/items/{iid}", pid, iid)
                    .exchange()
                    /* Then */
                    .expectStatus().isOk
                    .expectBody()
                    .assertThatJson {
                        isEqualTo(""" {
                           "cover":{
                              "height":200,
                              "id":"f4efe8db-7abf-4998-b15c-9fa2e06096a1",
                              "url":"https://external.domain.tld/foo/bar.png",
                              "width":200
                           },
                           "creationDate":"2019-02-05T13:14:15Z",
                           "description":"desc",
                           "downloadDate":null,
                           "fileName":null,
                           "id":"27184b1a-7642-4ffd-ac7e-14fb36f7f15c",
                           "isDownloaded":false,
                           "length":100,
                           "mimeType":null,
                           "podcast":{
                              "id":"8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9",
                              "title":"Podcast Bar",
                              "url":"https://external.domain.tld/bar.rss"
                           },
                           "podcastId":"8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9",
                           "proxyURL":"/api/v1/podcasts/8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9/items/27184b1a-7642-4ffd-ac7e-14fb36f7f15c/Foo",
                           "pubDate":"2019-02-01T13:14:15Z",
                           "status":"NOT_DOWNLOADED",
                           "title":"Foo",
                           "url":"https://external.domain.tld/foo/bar.mp4"
                        } """)
                    }
        }

        @Test
        fun `with downloaded item`() {
            /* Given */
            val downloadedItem = notDownloadedItem.copy(
                    fileName = "foo.mp4",
                    mimeType = "video/mp4",
                    status = Status.FINISH
            )
            val pid = downloadedItem.podcast.id
            val iid = downloadedItem.id
            whenever(itemService.findById(iid)).thenReturn(downloadedItem.toMono())
            /* When */
            rest.get()
                    .uri("/api/v1/podcasts/{pid}/items/{iid}", pid, iid)
                    .exchange()
                    /* Then */
                    .expectStatus().isOk
                    .expectBody()
                    .assertThatJson {
                        isEqualTo(""" {
                           "cover":{
                              "height":200,
                              "id":"f4efe8db-7abf-4998-b15c-9fa2e06096a1",
                              "url":"https://external.domain.tld/foo/bar.png",
                              "width":200
                           },
                           "creationDate":"2019-02-05T13:14:15Z",
                           "description":"desc",
                           "downloadDate":null,
                           "fileName":"foo.mp4",
                           "id":"27184b1a-7642-4ffd-ac7e-14fb36f7f15c",
                           "isDownloaded":true,
                           "length":100,
                           "mimeType":"video/mp4",
                           "podcast":{
                              "id":"8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9",
                              "title":"Podcast Bar",
                              "url":"https://external.domain.tld/bar.rss"
                           },
                           "podcastId":"8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9",
                           "proxyURL":"/api/v1/podcasts/8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9/items/27184b1a-7642-4ffd-ac7e-14fb36f7f15c/Foo.mp4",
                           "pubDate":"2019-02-01T13:14:15Z",
                           "status":"FINISH",
                           "title":"Foo",
                           "url":"https://external.domain.tld/foo/bar.mp4"
                        } """)
                    }
        }


    }

    @Nested
    @DisplayName("should reset")
    inner class ShouldReset {

        private val anItemToBeReseted = Item(
                id = UUID.fromString("27184b1a-7642-4ffd-ac7e-14fb36f7f15c"),
                title = "Foo",
                url = "https://external.domain.tld/foo/bar.mp4",

                pubDate = OffsetDateTime.parse("2019-02-01T13:14:15.000Z"),
                downloadDate = null,
                creationDate = OffsetDateTime.parse("2019-02-05T13:14:15.000Z"),

                description = "desc",
                mimeType = null,
                length = 100,
                fileName = null,
                status = Status.NOT_DOWNLOADED,

                podcast = PodcastForItem(
                        id = UUID.fromString("8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9"),
                        title = "Podcast Bar",
                        url = "https://external.domain.tld/bar.rss"
                ),
                cover = CoverForItem(
                        id = UUID.fromString("f4efe8db-7abf-4998-b15c-9fa2e06096a1"),
                        url = "https://external.domain.tld/foo/bar.png",
                        width = 200,
                        height = 200
                )
        )


        @Test
        fun `an item`() {
            /* Given */
            whenever(itemService.reset(anItemToBeReseted.id)).thenReturn(anItemToBeReseted.toMono())
            /* When */
            rest.post()
                    .uri("/api/v1/podcasts/${anItemToBeReseted.podcast.id}/items/${anItemToBeReseted.id}/reset")
                    .exchange()
                    /* Then */
                    .expectStatus().isOk
                    .expectBody()
                    .assertThatJson {
                        isEqualTo(""" {
                           "cover":{
                              "height":200,
                              "id":"f4efe8db-7abf-4998-b15c-9fa2e06096a1",
                              "url":"https://external.domain.tld/foo/bar.png",
                              "width":200
                           },
                           "creationDate":"2019-02-05T13:14:15Z",
                           "description":"desc",
                           "downloadDate":null,
                           "fileName":null,
                           "id":"27184b1a-7642-4ffd-ac7e-14fb36f7f15c",
                           "isDownloaded":false,
                           "length":100,
                           "mimeType":null,
                           "podcast":{
                              "id":"8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9",
                              "title":"Podcast Bar",
                              "url":"https://external.domain.tld/bar.rss"
                           },
                           "podcastId":"8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9",
                           "proxyURL":"/api/v1/podcasts/8e2df56f-959b-4eb4-b5fa-0fd6027ae0f9/items/27184b1a-7642-4ffd-ac7e-14fb36f7f15c/Foo",
                           "pubDate":"2019-02-01T13:14:15Z",
                           "status":"NOT_DOWNLOADED",
                           "title":"Foo",
                           "url":"https://external.domain.tld/foo/bar.mp4"
                        } """)
                    }
        }

    }

}
