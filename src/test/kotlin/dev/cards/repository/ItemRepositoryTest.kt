package dev.cards.repository

import dev.cards.AbstractTest
import dev.cards.domain.Item
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import reactor.test.StepVerifier
import java.time.Instant


/**
 * @author vladov 2019-03-16
 */
@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemRepositoryTest(@Autowired private val itemRepository: ItemRepository) : AbstractTest() {

    @Test
    fun testFindByContentContainingAndUserIdSuccess() {
        itemRepository.save(Item(null, "desc", "132sda", Instant.now(), "userid1")).block()
        itemRepository.save(Item(null, "addescda", "sda321", Instant.now(), "userid1")).block()
        val items = itemRepository.findByContentContainingAndUserId("desc", "userid1")
        StepVerifier
                .create(items)
                .assertNext { item ->
                    assertThat(item).isNotNull
                    assertThat(item.content).isEqualTo("desc")
                }
                .assertNext { item ->
                    assertThat(item.content).isEqualTo("addescda")
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun testFindByDescriptionContainingAndUsernameEmptyResult() {
        val items = itemRepository.findByContentContainingAndUserId("desc", "vasya")
        StepVerifier
                .create(items)
                .expectComplete()
                .verify()
    }

    @Test
    fun testFindFirstByUserIdSuccess() {
        itemRepository.save(Item(null, "desc", "132sda", Instant.now(), "userid2")).block()

        val item = itemRepository.findFirstByUserId("userid2")
        StepVerifier
                .create(item)
                .assertNext { i ->
                    assertThat(i.content).isEqualTo("desc")
                    assertThat(i.type).isEqualTo("132sda")
                    assertThat(i.timestamp).isNotNull()
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun testFindByIdSuccess() {
        itemRepository.save(Item("123321", "dasda", "das", Instant.now(), "userid3")).block()
        val items = itemRepository.findById("123321")
        StepVerifier
                .create(items)
                .assertNext { item ->
                    assertThat(item).isNotNull
                    assertThat(item.id).isEqualTo("123321")
                    assertThat(item.content).isEqualTo("dasda")
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun testFindByIdEmptyResult() {
        val items = itemRepository.findById("1")
        StepVerifier
                .create(items)
                .expectComplete()
                .verify()
    }

    @Test
    fun testFindByTypeAndUserIdSuccess() {
        itemRepository.save(Item("123321", "dasda", "type", Instant.now(), "userid3")).block()
        val items = itemRepository.findByTypeAndUserId("type", "userid3")
        StepVerifier
                .create(items)
                .assertNext { item ->
                    assertThat(item).isNotNull
                    assertThat(item.id).isEqualTo("123321")
                    assertThat(item.content).isEqualTo("dasda")
                    assertThat(item.type).isEqualTo("type")
                    assertThat(item.userId).isEqualTo("userid3")
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun testFindByTypeAndUserIdEmptyResult() {
        itemRepository.save(Item("123321", "dasda", "type", Instant.now(), "userid3")).block()
        val items = itemRepository.findByTypeAndUserId("123321", "userid3");
        StepVerifier
                .create(items)
                .expectComplete()
                .verify()
    }
}
