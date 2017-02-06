package nl.brightbits.lagom.shoppingbasket.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import javax.annotation.concurrent.Immutable;

public interface ShoppingBasketEvent extends Jsonable, AggregateEvent<ShoppingBasketEvent> {

    @Override
    default public AggregateEventTag<ShoppingBasketEvent> aggregateTag() {
        return ShoppingBasketEventTag.INSTANCE;
    }

    @Immutable
    @JsonDeserialize
    @Value
    @Builder
    @Wither
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    final class ShoppingBasketCreated implements ShoppingBasketEvent {
        @NonNull
        String id;
        @NonNull
        String shopId;
        @NonNull
        String customerId;
    }


    @Immutable
    @JsonDeserialize
    @Value
    @Builder
    @Wither
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    final class ItemAddedToShoppingBasket implements ShoppingBasketEvent {
        @NonNull
        String shoppingBasketId;
        @NonNull
        String shopId;
        @NonNull
        String customerId;
        @NonNull
        String skuId;
        @NonNull
        Integer initialAmount;
    }

    @Immutable
    @JsonDeserialize
    @Value
    @Builder
    @Wither
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    final class ItemAmountUpdatedInShoppingBasket implements ShoppingBasketEvent {
        @NonNull
        String shoppingBasketId;
        @NonNull
        String skuId;
        @NonNull
        Integer newAmount;
    }

    @Immutable
    @JsonDeserialize
    @Value
    @Builder
    @Wither
    @AllArgsConstructor(onConstructor = @__(@JsonCreator))
    final class ItemRemovedFromShoppingBasket implements ShoppingBasketEvent {
        @NonNull
        String shoppingBasketId;
        @NonNull
        String skuId;
    }
}
