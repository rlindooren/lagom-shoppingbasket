package nl.brightbits.lagom.shoppingbasket.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasket;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasketItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class ShoppingBasketEntityTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create(ShoppingBasketEntityTest.class.getSimpleName());
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testCreateShoppingBasket() {
        final String shopId = "1";
        final String customerId = "1";
        final String shoppingBasketId = UUID.randomUUID().toString();

        PersistentEntityTestDriver<ShoppingBasketCommand, ShoppingBasketEvent, ShoppingBasketState> driver =
                new PersistentEntityTestDriver<>(system, new ShoppingBasketEntity(), shoppingBasketId);

        PersistentEntityTestDriver.Outcome<ShoppingBasketEvent, ShoppingBasketState> outcome;

        // Create a shopping basket for the identifier
        outcome = driver.run(new ShoppingBasketCommand.CreateShoppingBasket(shopId, customerId));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(shoppingBasketId, outcome.getReplies().get(0));

        // Creating a shopping basket for the second with the same identifier time should fail
        outcome = driver.run(new ShoppingBasketCommand.CreateShoppingBasket(shopId, customerId));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals("Shoppingbasket " + shoppingBasketId + " has already been created",
                ((PersistentEntity.InvalidCommandException)outcome.getReplies().get(0)).message());
    }

    @Test
    public void testAddItemInShoppingBasket() {
        final String shopId = "1";
        final String customerId = "1";
        final String shoppingBasketId = UUID.randomUUID().toString();

        PersistentEntityTestDriver<ShoppingBasketCommand, ShoppingBasketEvent, ShoppingBasketState> driver =
                new PersistentEntityTestDriver<>(system, new ShoppingBasketEntity(), shoppingBasketId);

        // Create the shopping basket
        driver.run(new ShoppingBasketCommand.CreateShoppingBasket(shopId, customerId));

        final String skuId = "12345_a";

        PersistentEntityTestDriver.Outcome<ShoppingBasketEvent, ShoppingBasketState> outcome;

        // Adding 0 items should fail
        outcome = driver.run(new ShoppingBasketCommand.AddItemInShoppingBasket(skuId, 0));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals("Should at least add one",
                ((PersistentEntity.InvalidCommandException)outcome.getReplies().get(0)).message());

        // Adding 2 items
        outcome = driver.run(new ShoppingBasketCommand.AddItemInShoppingBasket(skuId, 2));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(Done.getInstance(), outcome.getReplies().get(0));

        // Check that is has been added to the state
        outcome = driver.run(new ShoppingBasketCommand.GetShoppingBasket());
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(1, ((ShoppingBasket)outcome.getReplies().get(0)).getItems().get().size());
        ShoppingBasketItem item = ((ShoppingBasket)outcome.getReplies().get(0)).getItems().get().get(0);
        assertEquals(skuId, item.getSkuId());
        assertEquals(2, item.getAmount());

        // Adding the same product again should fail
        outcome = driver.run(new ShoppingBasketCommand.AddItemInShoppingBasket(skuId, 1));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals("Item has already been added, please update amount only",
                ((PersistentEntity.InvalidCommandException)outcome.getReplies().get(0)).message());
    }

    @Test
    public void testAddUpdateItemAmountInShoppingBasket() {
        final String shopId = "1";
        final String customerId = "1";
        final String shoppingBasketId = UUID.randomUUID().toString();

        PersistentEntityTestDriver<ShoppingBasketCommand, ShoppingBasketEvent, ShoppingBasketState> driver =
                new PersistentEntityTestDriver<>(system, new ShoppingBasketEntity(), shoppingBasketId);

        // Create the shopping basket
        driver.run(new ShoppingBasketCommand.CreateShoppingBasket(shopId, customerId));

        final String skuId = "12345_a";

        PersistentEntityTestDriver.Outcome<ShoppingBasketEvent, ShoppingBasketState> outcome;

        // Updating a product that doesn't exist in the shopping basket should fail
        outcome = driver.run(new ShoppingBasketCommand.UpdateItemAmountInShoppingBasket(skuId, 2));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals("Item has not yet been added, please add it first",
                ((PersistentEntity.InvalidCommandException)outcome.getReplies().get(0)).message());

        // Add the product
        driver.run(new ShoppingBasketCommand.AddItemInShoppingBasket(skuId, 1));

        // Update the amount
        outcome = driver.run(new ShoppingBasketCommand.UpdateItemAmountInShoppingBasket(skuId, 2));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(Done.getInstance(), outcome.getReplies().get(0));

        // Check that there is still one item only
        outcome = driver.run(new ShoppingBasketCommand.GetShoppingBasket());
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(1, ((ShoppingBasket)outcome.getReplies().get(0)).getItems().get().size());
        ShoppingBasketItem item = ((ShoppingBasket)outcome.getReplies().get(0)).getItems().get().get(0);
        assertEquals(skuId, item.getSkuId());
        assertEquals(2, item.getAmount());
    }

    @Test
    public void testRemoveItemFromShoppingBasket() {
        final String shopId = "1";
        final String customerId = "1";
        final String shoppingBasketId = UUID.randomUUID().toString();

        PersistentEntityTestDriver<ShoppingBasketCommand, ShoppingBasketEvent, ShoppingBasketState> driver =
                new PersistentEntityTestDriver<>(system, new ShoppingBasketEntity(), shoppingBasketId);

        // Create the shopping basket
        driver.run(new ShoppingBasketCommand.CreateShoppingBasket(shopId, customerId));

        final String skuId = "12345_a";

        PersistentEntityTestDriver.Outcome<ShoppingBasketEvent, ShoppingBasketState> outcome;

        // Add the product
        driver.run(new ShoppingBasketCommand.AddItemInShoppingBasket(skuId, 1));

        // Check that is has been added to the state
        outcome = driver.run(new ShoppingBasketCommand.GetShoppingBasket());
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(1, ((ShoppingBasket)outcome.getReplies().get(0)).getItems().get().size());

        // Remove the product
        outcome = driver.run(new ShoppingBasketCommand.RemoveItemFromShoppingBasket(skuId));
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(Done.getInstance(), outcome.getReplies().get(0));

        // Check that it has been removed from the state
        outcome = driver.run(new ShoppingBasketCommand.GetShoppingBasket());
        assertTrue("There are issues: " + outcome.issues(), outcome.issues().isEmpty());
        assertEquals(0, ((ShoppingBasket)outcome.getReplies().get(0)).getItems().get().size());
    }
}
