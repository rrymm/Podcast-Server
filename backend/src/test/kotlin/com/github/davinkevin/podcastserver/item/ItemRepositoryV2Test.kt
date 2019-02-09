package com.github.davinkevin.podcastserver.item

import com.github.davinkevin.podcastserver.entity.Item
import com.ninja_squad.dbsetup.DbSetup
import com.ninja_squad.dbsetup.DbSetupTracker
import com.ninja_squad.dbsetup.destination.DataSourceDestination
import lan.dk.podcastserver.repository.DatabaseConfigurationTest.DELETE_ALL
import lan.dk.podcastserver.repository.DatabaseConfigurationTest.INSERT_ITEM_DATA
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import javax.sql.DataSource
import com.github.davinkevin.podcastserver.item.ItemRepositoryV2 as ItemRepository
import com.ninja_squad.dbsetup.operation.CompositeOperation.sequenceOf
import io.vavr.API
import jdk.nashorn.internal.objects.NativeArray.forEach
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.ThrowingExtractor
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import java.time.ZonedDateTime.now

@JooqTest
@Import(ItemRepository::class)
class ItemRepositoryV2Test {

    @Autowired lateinit var itemRepository: ItemRepository
    @Autowired lateinit var dataSource: DataSource

    private val dbSetupTracker = DbSetupTracker()

    @BeforeEach
    fun prepare() {
        val operation = sequenceOf(DELETE_ALL, INSERT_ITEM_DATA)
        val dbSetup = DbSetup(DataSourceDestination(dataSource), operation)

        dbSetupTracker.launchIfNecessary(dbSetup)
    }

    @Test
    fun should_find_all_to_download() {
        dbSetupTracker.skipNextLaunch()
        /* Given */
        val date = now().minusDays(15)

        /* When */
        val itemToDownload = itemRepository.findAllToDownload(date, 5)
        itemToDownload.forEach(::println)

        /* Then */
        assertThat(itemToDownload).hasSize(2)
        assertThat(itemToDownload)
                .extracting(ThrowingExtractor<Item, String, RuntimeException> { it.getTitle() })
                .contains("Appload 3", "Geek INC 122")
    }



}
