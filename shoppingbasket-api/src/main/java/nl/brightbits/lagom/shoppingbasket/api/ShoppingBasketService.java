package nl.brightbits.lagom.shoppingbasket.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface ShoppingBasketService extends Service {

    ServiceCall<CreateShoppingbasketRequest, String> createShoppingBasket();

    ServiceCall<NotUsed, ShoppingBasket> getShoppingBasket(String shoppingBasketId);

    ServiceCall<AddItemToShoppingbasketRequest, Done> addItemToShoppingBasket(String shoppingBasketId);

    ServiceCall<UpdateItemAmountInShoppingbasketRequest, Done> updateItemAmountInShoppingBasket(String shoppingBasketId);

    ServiceCall<RemoveItemFromShoppingbasketRequest, Done> removeItemFromShoppingBasket(String shoppingBasketId);

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("shoppingbasket").withCalls(
                restCall(Method.POST, "/api/shoppingbasket", this::createShoppingBasket),
                restCall(Method.GET, "/api/shoppingbasket/:shoppingBasketId", this::getShoppingBasket),
                restCall(Method.POST, "/api/shoppingbasket/:shoppingBasketId/items", this::addItemToShoppingBasket),
                restCall(Method.PUT, "/api/shoppingbasket/:shoppingBasketId/items", this::updateItemAmountInShoppingBasket),
                restCall(Method.DELETE, "/api/shoppingbasket/:shoppingBasketId/items", this::removeItemFromShoppingBasket)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
