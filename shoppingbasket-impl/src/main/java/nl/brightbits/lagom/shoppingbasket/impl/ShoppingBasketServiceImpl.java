package nl.brightbits.lagom.shoppingbasket.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import nl.brightbits.lagom.shoppingbasket.api.*;

import javax.inject.Inject;
import java.util.UUID;

public class ShoppingBasketServiceImpl implements ShoppingBasketService {

    private final PersistentEntityRegistry registry;

    @Inject
    public ShoppingBasketServiceImpl(final PersistentEntityRegistry registry, final ReadSide readSide) {
        this.registry = registry;
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
}
