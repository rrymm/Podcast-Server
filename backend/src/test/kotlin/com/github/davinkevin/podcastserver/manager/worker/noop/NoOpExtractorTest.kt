package com.github.davinkevin.podcastserver.manager.worker.noop

import lan.dk.podcastserver.entity.Item
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Created by kevin on 03/12/2017
 */
class NoOpExtractorTest {

    private val extractor = NoOpExtractor()

    @Test
    fun `should return item and its url as is`() {
        /* GIVEN */
        val item = Item().apply {
            url = "Foo"
        }

        /* WHEN  */
        val extractedValue = extractor.extract(item)

        /* THEN  */
        assertThat(extractedValue.item).isEqualTo(item)
        assertThat(extractedValue.urls).contains(item.url)
    }

    @Test
    fun `should return max compatibility`() {
        assertThat(extractor.compatibility("foo")).isEqualTo(Integer.MAX_VALUE)
    }

}