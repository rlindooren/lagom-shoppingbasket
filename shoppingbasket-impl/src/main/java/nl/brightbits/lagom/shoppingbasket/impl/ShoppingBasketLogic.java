package nl.brightbits.lagom.shoppingbasket.impl;

import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasket;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasketItem;
import org.pcollections.TreePVector;

import java.util.stream.Collectors;

public class ShoppingBasketLogic {

    /**
     * Can be used to add a new item or update the amount of an already existing item
     *
     * @param shoppingBasket
     * @param skuId          the unique identifier of the item/product
     * @param amount         the number of products to add
     * @return a new immutable shopping basket with the added item
     */
    public static ShoppingBasket addItemToShoppingBasket(ShoppingBasket shoppingBasket, String skuId, int amount) {
        ShoppingBasket shoppingBasketWithoutItem = removeItemFromShoppingBasket(shoppingBasket, skuId);
        return shoppingBasketWithoutItem.withItems(
                shoppingBasketWithoutItem.getItems().plus(new ShoppingBasketItem(skuId, amount))
        );
    }

    /**
     * @param shoppingBasket
     * @param skuId          the unique identifier of the item/product
     * @return a new immutable shopping basket without the removed item
     */
    public static ShoppingBasket removeItemFromShoppingBasket(ShoppingBasket shoppingBasket, String skuId) {
        return shoppingBasket.withItems(
                TreePVector.from(
                        shoppingBasket.getItems()
                                .stream().filter(shoppingBasketItem -> !shoppingBasketItem.getSkuId().equals(skuId))
                                .collect(Collectors.toList())
                )
        );
    }

    public static int getAmountForSku(ShoppingBasket shoppingBasket, String skuId) {
        return shoppingBasket.getItems()
                .stream().filter(shoppingBasketItem -> shoppingBasketItem.getSkuId().equals(skuId))
                .map(ShoppingBasketItem::getAmount)
                .reduce((total, itemAmount) -> total + itemAmount).orElse(0);
    }
}
