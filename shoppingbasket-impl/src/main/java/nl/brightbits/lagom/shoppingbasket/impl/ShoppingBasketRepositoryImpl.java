package nl.brightbits.lagom.shoppingbasket.impl;


import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasketItem;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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

    @Override
    public CompletionStage<PSequence<ShoppingBasketItem>> getShoppingBasketItems(UUID shoppingBasketId) {
        return db.selectAll("SELECT * FROM " + ShoppingBasketEventProcessor.TABLE_NAME_SHOPPINGBASKET_ITEM +
                " WHERE shoppingBasketId=?", shoppingBasketId)
                .thenApply(items -> TreePVector.from(
                        items.stream()
                                .map(row -> ShoppingBasketItem
                                        .builder()
                                        .amount(row.getInt("amount"))
                                        .skuId(row.getString("skuId"))
                                        .build()).collect(Collectors.toList())
                ));
    }
}
