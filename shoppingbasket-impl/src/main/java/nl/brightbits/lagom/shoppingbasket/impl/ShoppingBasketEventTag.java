package nl.brightbits.lagom.shoppingbasket.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class ShoppingBasketEventTag {

    public static final AggregateEventTag<ShoppingBasketEvent> INSTANCE =
            AggregateEventTag.of(ShoppingBasketEvent.class);

}
