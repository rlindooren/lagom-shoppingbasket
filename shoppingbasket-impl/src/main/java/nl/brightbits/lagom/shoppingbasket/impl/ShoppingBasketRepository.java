package nl.brightbits.lagom.shoppingbasket.impl;

import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasketItem;
import org.pcollections.PSequence;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface ShoppingBasketRepository {

    CompletionStage<Optional<UUID>> getIdOfMostRecentShoppingBasket(final String shopId, final String customerId);

    CompletionStage<PSequence<ShoppingBasketItem>> getShoppingBasketItems(final UUID shoppingBasketId);
}
