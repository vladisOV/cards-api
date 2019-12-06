package dev.cards.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.cards.domain.Item
import dev.cards.repository.ItemRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.time.ZoneOffset


/**
 * @author vladov 2019-03-16
 */
@ExtendWith(SpringExtension::class)
@WebMvcTest(ItemController::class)
@EnableSpringDataWebSupport
@ActiveProfiles("test")
@Disabled
//TODO fix
class ItemControllerTest {

    @MockBean
    private lateinit var itemRepository: ItemRepository
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var webClient: WebTestClient
    private lateinit var item: Item
    private lateinit var itemJson: String

    @BeforeEach
    fun setup() {
        webClient = WebTestClient
                .bindToController(ItemController(itemRepository))
                .apply<WebTestClient.ControllerSpec>(SecurityMockServerConfigurers.springSecurity())
                .build()

        item = Item("123", "header", "desc", "res",
                LocalDateTime.of(2019, 1, 1, 0, 0, 0)
                        .toInstant(ZoneOffset.UTC), "userId")
        itemJson = objectMapper.writeValueAsString(item)
        val itemFlux = Flux.just(item)

        given(itemRepository.save(item)).willReturn(Mono.just(item))
        given(itemRepository.findAllByUserId("user")).willReturn(itemFlux)
        given(itemRepository.findByContentContainingAndUserId("desc", "user")).willReturn(itemFlux)
    }

    @Test
    fun testGetAllItemsSuccess() {
        val result: FluxExchangeResult<Item> = webClient.get().uri("/api/item")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .returnResult(Item::class.java)

        val itemsFlux: Flux<Item> = result.responseBody
        StepVerifier
                .create(itemsFlux)
                .expectSubscription()
                .assertNext { item ->
                    assertThat(item).isNotNull
                    assertThat(item.id).isEqualTo("123")
                    assertThat(item.content).isEqualTo("desc")
                }
                .expectComplete()
                .verify()
    }


    @Test
    fun testSaveItemSuccess() {
        val result: FluxExchangeResult<Item> = webClient.post()
                .uri("/api/item")
                .syncBody(item)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .returnResult(Item::class.java)

        val itemsFlux: Flux<Item> = result.responseBody
        StepVerifier
                .create(itemsFlux)
                .expectSubscription()
                .assertNext { item ->
                    assertThat(item).isNotNull
                    assertThat(item.id).isEqualTo("123")
                    assertThat(item.content).isEqualTo("desc")
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun testFindByDescriptionContainingSuccess() {
        val result: FluxExchangeResult<Item> = webClient.get()
                .uri("/api/item")
                .attribute("description", item.content)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .returnResult(Item::class.java)

        val itemsFlux: Flux<Item> = result.responseBody
        StepVerifier
                .create(itemsFlux)
                .expectSubscription()
                .assertNext { item ->
                    assertThat(item).isNotNull
                    assertThat(item.id).isEqualTo("123")
                    assertThat(item.content).isEqualTo("desc")
                }
                .expectComplete()
                .verify()
    }
}

