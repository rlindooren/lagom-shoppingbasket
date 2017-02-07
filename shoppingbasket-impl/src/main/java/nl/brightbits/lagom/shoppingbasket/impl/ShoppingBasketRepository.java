package nl.brightbits.lagom.shoppingbasket.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface ShoppingBasketRepository {

    CompletionStage<Optional<UUID>> getIdOfMostRecentShoppingBasket(final String shopId, final String customerId);
}
