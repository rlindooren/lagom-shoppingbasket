package nl.brightbits.lagom.shoppingbasket.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;
import org.pcollections.PSequence;

import javax.annotation.concurrent.Immutable;
import java.util.Optional;

@Immutable
@JsonDeserialize
@Value
@Builder
@Wither
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ShoppingBasket implements Jsonable {

    @NonNull
    String id;

    @NonNull
    String shopId;

    @NonNull
    String customerId;

    @NonNull
    Optional<PSequence<ShoppingBasketItem>> items;
}
