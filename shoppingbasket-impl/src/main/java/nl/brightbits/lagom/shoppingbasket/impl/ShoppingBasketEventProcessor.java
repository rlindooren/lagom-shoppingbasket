package nl.brightbits.lagom.shoppingbasket.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import nl.brightbits.lagom.shoppingbasket.impl.ShoppingBasketEvent.ItemAddedToShoppingBasket;
import nl.brightbits.lagom.shoppingbasket.impl.ShoppingBasketEvent.ItemAmountUpdatedInShoppingBasket;
import nl.brightbits.lagom.shoppingbasket.impl.ShoppingBasketEvent.ItemRemovedFromShoppingBasket;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;

public class ShoppingBasketEventProcessor extends ReadSideProcessor<ShoppingBasketEvent> {

    public static final String TABLE_NAME = "shoppingbasket";
    private final CassandraSession session;
    private final CassandraReadSide readSide;
    private PreparedStatement insertShoppingBasketItemPs = null;
    private PreparedStatement updatedShoppingBasketItemPs = null;
    private PreparedStatement deleteShoppingBasketItemPs = null;

    @Inject
    public ShoppingBasketEventProcessor(CassandraSession session, CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    @Override
    public PSequence<AggregateEventTag<ShoppingBasketEvent>> aggregateTags() {
        return TreePVector.singleton(ShoppingBasketEventTag.INSTANCE);
    }

    @Override
    public ReadSideHandler buildHandler() {
        return readSide.<ShoppingBasketEvent>builder("shoppingbasket_offset")
                .setGlobalPrepare(this::prepareCreateTables)
                .setPrepare((ignored) -> prepareStatements())
                .setEventHandler(ItemAddedToShoppingBasket.class, this::processItemAddedToShoppingBasket)
                .setEventHandler(ItemAmountUpdatedInShoppingBasket.class, this::processItemAmountUpdatedInShoppingBasket)
                .setEventHandler(ItemRemovedFromShoppingBasket.class, this::processItemRemovedFromShoppingBasket)
                .build();
    }

    private CompletionStage<Done> prepareCreateTables() {
        // @formatter:off
        return session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        "id uuid, " +
                        "shopId text, " +
                        "customerId text, " +
                        "skuId text, " +
                        "amount int, " +
                        "lastUpdated timestamp, " +
                        "PRIMARY KEY (id, skuId)" +
                        ")");
        // @formatter:on
    }

    private CompletionStage<Done> prepareStatements() {
        CompletionStage<Done> insert = session.prepare("INSERT INTO " + TABLE_NAME + " " +
                "(id, shopId, customerId, skuId, amount) VALUES (?, ?, ?, ?, ?)")
                .thenApply(ps -> {
                    insertShoppingBasketItemPs = ps;
                    return Done.getInstance();
                });

        CompletionStage<Done> update = session.prepare("UPDATE " + TABLE_NAME + " " +
                "SET amount=? where id=? and skuId=?")
                .thenApply(ps -> {
                    updatedShoppingBasketItemPs = ps;
                    return Done.getInstance();
                });

        CompletionStage<Done> delete = session.prepare("DELETE FROM " + TABLE_NAME + " where id=? and skuId=?")
                .thenApply(ps -> {
                    deleteShoppingBasketItemPs = ps;
                    return Done.getInstance();
                });

        return insert
                .thenCombine(update, ((done, done2) -> done))
                .thenCombine(delete, ((done, done2) -> done));
    }

    private CompletionStage<List<BoundStatement>> processItemAddedToShoppingBasket(ItemAddedToShoppingBasket event) {
        BoundStatement bindInsertItem = insertShoppingBasketItemPs.bind()
                .setUUID("id", UUID.fromString(event.getShoppingBasketId()))
                .setString("shopId", event.getShopId())
                .setString("customerId", event.getCustomerId())
                .setString("skuId", event.getSkuId())
                .setInt("amount", event.getInitialAmount());
        return completedStatement(bindInsertItem);
    }

    private CompletionStage<List<BoundStatement>> processItemAmountUpdatedInShoppingBasket(
            ItemAmountUpdatedInShoppingBasket event) {
        BoundStatement bindUpdateItem = updatedShoppingBasketItemPs.bind()
                .setString("id", event.getShoppingBasketId())
                .setString("skuId", event.getSkuId())
                .setInt("amount", event.getNewAmount());
        return completedStatement(bindUpdateItem);
    }

    private CompletionStage<List<BoundStatement>> processItemRemovedFromShoppingBasket(
            ItemRemovedFromShoppingBasket event) {
        BoundStatement bindDeleteItem = deleteShoppingBasketItemPs.bind()
                .setString("id", event.getShoppingBasketId())
                .setString("skuId", event.getSkuId());
        return completedStatement(bindDeleteItem);
    }
}
