package nl.brightbits.lagom.shoppingbasket.impl;


import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ShoppingBasketRepositoryImpl implements ShoppingBasketRepository {

    private CassandraSession db;

    @Inject
    public ShoppingBasketRepositoryImpl(CassandraSession db) {
        this.db = db;
    }

    @Override
    public CompletionStage<Optional<UUID>> getIdOfMostRecentShoppingBasket(final String shopId,
                                                                           final String customerId) {
        return db.selectAll("SELECT id FROM " + ShoppingBasketEventProcessor.TABLE_NAME_SHOPPINGBASKET +
                " WHERE shopId=? and customerId=? LIMIT 1", shopId, customerId)
                .thenApply(ids -> ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0).getUUID("id")));
    }
}
