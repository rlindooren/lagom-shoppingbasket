package nl.brightbits.lagom.shoppingbasket.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import javax.annotation.concurrent.Immutable;

@Immutable
@JsonDeserialize
@Value
@Builder
@Wither
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class CreateShoppingbasketRequest {

    @NonNull
    String shopId;

    @NonNull
    String customerId;

}
