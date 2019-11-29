package dev.cards.controller

import dev.cards.domain.Item
import dev.cards.repository.ItemRepository
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author vladov 2019-03-14
 */
@RestController("api/item")
class ItemController(private val itemRepository: ItemRepository) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun findAll(@AuthenticationPrincipal userId: String): Flux<Item> {
        return itemRepository.findAllByUserId(userId)
    }

    @GetMapping(params = ["id"])
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun findById(@RequestParam id: String): Mono<Item> = itemRepository.findById(id)


    @GetMapping(params = ["content"])
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun findByDescription(@RequestParam content: String, @AuthenticationPrincipal(expression = "id") username: String):
            Flux<Item> = itemRepository.findByContentContainingAndUserId(content, username)

    @GetMapping(params = ["type"])
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun findByType(@RequestParam type: String, @AuthenticationPrincipal userId: String):
            Flux<Item> = itemRepository.findByTypeAndUserId(type, userId)


    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun saveItem(@RequestBody item: Item, @AuthenticationPrincipal userId: String): Mono<Item> {
        item.userId = userId
        return itemRepository.save(item)
    }
}
