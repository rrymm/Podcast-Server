package com.github.davinkevin.podcastserver.item

import com.github.davinkevin.podcastserver.entity.Status
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.util.*

/**
 * Created by kevin on 2019-02-09
 */
data class DeleteItemInformation(val id: UUID, val fileName: String, val podcastTitle: String) {
    val path = Paths.get(podcastTitle, fileName)!!
}

data class Item(
        val id: UUID,
        val title: String,
        val url: String,

        val pubDate: OffsetDateTime?,
        val downloadDate: OffsetDateTime?,
        val creationDate: OffsetDateTime?,

        val description: String?,
        val mimeType: String?,
        val length: Long?,
        val fileName: String?,
        val status: Status,

        val podcast: PodcastForItem,
        val cover: CoverForItem
) {
    fun isDownloaded() = Status.FINISH == status
}

data class PodcastForItem(
        val id: UUID,
        val title: String,
        val url: String
)

data class CoverForItem (
        val id: UUID,
        val url: String,
        val width: Int,
        val height: Int
)

data class ItemSort(val direction: String, val field: String)
data class ItemPageRequest(val page: Int, val size: Int, val sort: ItemSort)

data class PageItem(
        val content: Collection<Item>,
        val empty: Boolean,
        val first: Boolean,
        val last: Boolean,
        val number: Int,
        val numberOfElements: Int,
        val size: Int,
        val totalElements: Int,
        val totalPages: Int
) {
    companion object {

        fun of(content: Collection<Item>, totalElements: Int, page: ItemPageRequest): PageItem {

            val totalPages = Math.ceil(totalElements.toDouble() / page.size.toDouble()).toInt()

            return PageItem(
                    content = content,
                    empty = content.isEmpty(),
                    first = page.page == 0,
                    last = page.page + 1 > totalPages - 1,
                    number = page.page,
                    numberOfElements = content.size,
                    size = page.size,
                    totalElements = totalElements,
                    totalPages = totalPages
            )

        }

    }
}
