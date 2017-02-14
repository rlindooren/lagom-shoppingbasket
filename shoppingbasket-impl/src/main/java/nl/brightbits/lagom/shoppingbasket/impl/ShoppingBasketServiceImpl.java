package nl.brightbits.lagom.shoppingbasket.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import nl.brightbits.lagom.shoppingbasket.api.*;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.util.UUID;

public class ShoppingBasketServiceImpl implements ShoppingBasketService {

    private final PersistentEntityRegistry registry;
    private final ShoppingBasketRepository shoppingBasketRepository;

    @Inject
    public ShoppingBasketServiceImpl(final PersistentEntityRegistry registry,
                                     final ReadSide readSide,
                                     final ShoppingBasketRepository shoppingBasketRepository) {
        this.registry = registry;
        this.shoppingBasketRepository = shoppingBasketRepository;
        registry.register(ShoppingBasketEntity.class);
        readSide.register(ShoppingBasketEventProcessor.class);
    }

    @Override
    public ServiceCall<CreateShoppingbasketRequest, String> createShoppingBasket() {
        return request -> registry.refFor(ShoppingBasketEntity.class, UUID.randomUUID().toString())
                .ask(new ShoppingBasketCommand.CreateShoppingBasket(
                        request.getShopId(),
                        request.getCustomerId()));
    }

    @Override
    public ServiceCall<NotUsed, ShoppingBasket> getShoppingBasket(final String shoppingBasketId) {
        return request -> registry.refFor(ShoppingBasketEntity.class, shoppingBasketId)
                .ask(new ShoppingBasketCommand.GetShoppingBasket());
    }

    @Override
    public ServiceCall<AddItemToShoppingbasketRequest, Done> addItemToShoppingBasket(final String shoppingBasketId) {
        return request -> registry.refFor(ShoppingBasketEntity.class, shoppingBasketId)
                .ask(new ShoppingBasketCommand.AddItemInShoppingBasket(
                        request.getSkuId(),
                        request.getInitialAmount()));
    }

    @Override
    public ServiceCall<UpdateItemAmountInShoppingbasketRequest, Done> updateItemAmountInShoppingBasket(final String shoppingBasketId) {
        return request -> registry.refFor(ShoppingBasketEntity.class, shoppingBasketId)
                .ask(new ShoppingBasketCommand.UpdateItemAmountInShoppingBasket(
                        request.getSkuId(),
                        request.getNewAmount()));
    }

    @Override
    public ServiceCall<RemoveItemFromShoppingbasketRequest, Done> removeItemFromShoppingBasket(final String shoppingBasketId) {
        return request -> registry.refFor(ShoppingBasketEntity.class, shoppingBasketId)
                .ask(new ShoppingBasketCommand.RemoveItemFromShoppingBasket(
                        request.getSkuId()));
    }

    @Override
    public ServiceCall<GetMostRecentShoppingbasketRequest, String> getMostRecentShoppingBasket() {
        return request ->
                shoppingBasketRepository.getIdOfMostRecentShoppingBasket(request.getShopId(), request.getCustomerId())
                        .thenApply(uuid -> uuid
                                .orElseThrow(() -> new NotFound("User doesn't have any shopping baskets"))
                                .toString());
    }

    @Override
    public ServiceCall<NotUsed, PSequence<ShoppingBasketItem>> getShoppingBasketItems(String shoppingBasketId) {
        return request ->
                shoppingBasketRepository.getShoppingBasketItems(UUID.fromString(shoppingBasketId));
    }
}
