package nl.brightbits.lagom.shoppingbasket.impl;

import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasket;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasketItem;
import org.pcollections.PSequence;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Optional;
import java.util.OptionalInt;

public class ShoppingBasketLogic {

    /**
     * Can be used to add a new item or update the amount of an already existing item
     *
     * @param shoppingBasket
     * @param skuId the unique identifier of the item/product
     * @param amount the number of products to add
     * @return a new immutable shopping basket with the added item
     */
    public static ShoppingBasket addItemToShoppingBasket(ShoppingBasket shoppingBasket, String skuId, int amount) {

        ShoppingBasketItem newItem = ShoppingBasketItem.builder()
                .skuId(skuId)
                .amount(amount)
                .build();

        PSequence<ShoppingBasketItem> newItems;

        OptionalInt index = getPositionOfItem(shoppingBasket, skuId);
        if (index.isPresent()) {
            // Update already existing item with new amount
            newItems = shoppingBasket.getItems().get().with(index.getAsInt(), newItem);
        } else {
            // Add new item
            newItems = shoppingBasket.getItems().orElse(TreePVector.empty()).plus(newItem);
        }

        return shoppingBasket.withItems(Optional.of(newItems));
    }

    /**
     *
     * @param shoppingBasket
     * @param skuId the unique identifier of the item/product
     * @return a new immutable shopping basket with the added item
     */
    public static ShoppingBasket removeItemFromShoppingBasket(ShoppingBasket shoppingBasket, String skuId) {

        OptionalInt index = getPositionOfItem(shoppingBasket, skuId);
        if (!index.isPresent()) {
            return shoppingBasket;
        }

        PSequence<ShoppingBasketItem> newItems = shoppingBasket.getItems().get().minus(index.getAsInt());

        return shoppingBasket.withItems(Optional.of(newItems));
    }

    public static OptionalInt getAmountForSku(ShoppingBasket shoppingBasket, String skuId) {
        OptionalInt index = getPositionOfItem(shoppingBasket, skuId);
        if (!index.isPresent()) {
            return OptionalInt.empty();
        } else {
            return OptionalInt.of(shoppingBasket.getItems().get().get(index.getAsInt()).getAmount());
        }
    }

    private static OptionalInt getPositionOfItem(ShoppingBasket shoppingBasket, String skuId) {
        if (!shoppingBasket.getItems().isPresent()) {
            return OptionalInt.empty();
        }

        int index = -1;
        for (int i=0; i < shoppingBasket.getItems().get().size(); i++) {
            if (shoppingBasket.getItems().get().get(i).getSkuId().equals(skuId)){
                index = i;
                break;
            }
        }

        return index == -1 ? OptionalInt.empty() : OptionalInt.of(index);
    }
}
