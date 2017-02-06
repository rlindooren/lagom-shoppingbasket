package nl.brightbits.lagom.shoppingbasket.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasket;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
@JsonDeserialize
@Value
@Builder
@Wither
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ShoppingBasketState implements Jsonable {

    @NonNull
    Optional<ShoppingBasket> shoppingBasket;
}
