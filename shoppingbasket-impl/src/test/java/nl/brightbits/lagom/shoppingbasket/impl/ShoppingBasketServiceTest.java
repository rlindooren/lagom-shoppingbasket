package nl.brightbits.lagom.shoppingbasket.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import nl.brightbits.lagom.shoppingbasket.api.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ShoppingBasketServiceTest {

    private static ServiceTest.TestServer server;

    @BeforeClass
    public static void setUp() {
        server = startServer(defaultSetup()
                .withCluster(false)
                .withCassandra(true));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testNormalFlow() throws Exception {
        ShoppingBasketService service = server.client(ShoppingBasketService.class);

        final String shopId = "1";
        final String customerId = "1";

        // Create the shopping basket
        CreateShoppingbasketRequest createShoppingbasketRequest = CreateShoppingbasketRequest.builder()
                .shopId(shopId)
                .customerId(customerId)
                .build();
        final String shoppingBasketId = service.createShoppingBasket()
                .invoke(createShoppingbasketRequest)
                .toCompletableFuture().get(5, SECONDS);
        assertTrue(StringUtils.isNotBlank(shoppingBasketId));

        // Add an item
        final String skuId = "1234_a";
        AddItemToShoppingbasketRequest addItemToShoppingbasketRequest = AddItemToShoppingbasketRequest.builder()
                .skuId(skuId)
                .initialAmount(1)
                .build();
        assertEquals(Done.getInstance(), service.addItemToShoppingBasket(shoppingBasketId)
                .invoke(addItemToShoppingbasketRequest)
                .toCompletableFuture()
                .get(5, SECONDS));

        // Update amount of item
        UpdateItemAmountInShoppingbasketRequest updateItemAmountInShoppingbasketRequest =
                UpdateItemAmountInShoppingbasketRequest.builder()
                        .skuId(skuId)
                        .newAmount(2)
                        .build();
        assertEquals(Done.getInstance(), service.updateItemAmountInShoppingBasket(shoppingBasketId)
                .invoke(updateItemAmountInShoppingbasketRequest)
                .toCompletableFuture()
                .get(5, SECONDS));

        // Add another item
        final String skuId2 = "5678_b";
        AddItemToShoppingbasketRequest addItemToShoppingbasketRequest2 = AddItemToShoppingbasketRequest.builder()
                .skuId(skuId2)
                .initialAmount(1)
                .build();
        assertEquals(Done.getInstance(), service.addItemToShoppingBasket(shoppingBasketId)
                .invoke(addItemToShoppingbasketRequest2)
                .toCompletableFuture().get(5, SECONDS));

        // Check contents of shopping basket
        ShoppingBasket shoppingBasket =
                service.getShoppingBasket(shoppingBasketId).invoke().toCompletableFuture().get(5, SECONDS);
        assertEquals(2, shoppingBasket.getItems().get().size());
        // TODO: use Hamcrest validators
        for (ShoppingBasketItem item : shoppingBasket.getItems().get()) {
            if (item.getSkuId().equals(skuId)) {
                assertEquals(2, item.getAmount());
            } else if (item.getSkuId().equals(skuId2)) {
                assertEquals(1, item.getAmount());
            }
        }

        // Remove one item
        RemoveItemFromShoppingbasketRequest removeItemFromShoppingbasketRequest = RemoveItemFromShoppingbasketRequest.builder()
                .skuId(skuId)
                .build();
        assertEquals(Done.getInstance(), service.removeItemFromShoppingBasket(shoppingBasketId)
                .invoke(removeItemFromShoppingbasketRequest)
                .toCompletableFuture()
                .get(5, SECONDS));

        // Check contents of shopping basket
        shoppingBasket =
                service.getShoppingBasket(shoppingBasketId).invoke().toCompletableFuture().get(5, SECONDS);
        assertEquals(1, shoppingBasket.getItems().get().size());
        // TODO: use Hamcrest validators
        for (ShoppingBasketItem item : shoppingBasket.getItems().get()) {
            if (item.getSkuId().equals(skuId)) {
                fail("Item shouldn't exist anymore");
            } else if (item.getSkuId().equals(skuId2)) {
                assertEquals(1, item.getAmount());
            }
        }
    }
}
