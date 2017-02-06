package nl.brightbits.lagom.shoppingbasket.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasket;

import java.util.Optional;
import java.util.OptionalInt;

public class ShoppingBasketEntity
        extends PersistentEntity<ShoppingBasketCommand, ShoppingBasketEvent, ShoppingBasketState> {

    @Override
    public Behavior initialBehavior(Optional<ShoppingBasketState> snapshotState) {
        final BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(
                ShoppingBasketState.builder().shoppingBasket(Optional.empty()).build()));
        addBehaviorForCreateShoppingBasket(b);
        addBehaviorForGetShoppingBasket(b);
        addBehaviorForAddItemToShoppingBasket(b);
        addBehaviorItemAmountUpdatedInShoppingBasket(b);
        addBehaviorForRemoveItemFromShoppingBasket(b);
        return b.build();
    }

    private void addBehaviorForCreateShoppingBasket(final BehaviorBuilder b) {
        b.setCommandHandler(ShoppingBasketCommand.CreateShoppingBasket.class,
                (cmd, ctx) -> {
                    if (state().getShoppingBasket().isPresent()) {
                        ctx.invalidCommand("Shoppingbasket " + entityId() + " has already been created");
                        return ctx.done();
                    } else {
                        return ctx.thenPersist(
                                ShoppingBasketEvent.ShoppingBasketCreated.builder()
                                        .id(entityId())
                                        .shopId(cmd.getShopId())
                                        .customerId(cmd.getCustomerId())
                                        .build(),
                                evt -> ctx.reply(entityId()));
                    }
                }
        );

        b.setEventHandler(ShoppingBasketEvent.ShoppingBasketCreated.class,
                evt -> new ShoppingBasketState(
                        Optional.of(
                                ShoppingBasket.builder()
                                        .id(evt.getId())
                                        .shopId(evt.getShopId())
                                        .customerId(evt.getCustomerId())
                                        .items(Optional.empty())
                                        .build()
                        ))
        );
    }

    private void addBehaviorForGetShoppingBasket(final BehaviorBuilder b) {
        b.setReadOnlyCommandHandler(ShoppingBasketCommand.GetShoppingBasket.class,
                (cmd, ctx) -> ctx.reply(state().getShoppingBasket().get()));
    }

    private void addBehaviorForAddItemToShoppingBasket(final BehaviorBuilder b) {
        b.setCommandHandler(ShoppingBasketCommand.AddItemInShoppingBasket.class,
                (cmd, ctx) -> {
                    if (!state().getShoppingBasket().isPresent()) {
                        ctx.invalidCommand("Shoppingbasket hasn't been created yet");
                        return ctx.done();
                    } else if (ShoppingBasketLogic.getAmountForSku(state().getShoppingBasket().get(), cmd.getSkuId()).isPresent()) {
                        ctx.invalidCommand("Item has already been added, please update amount only");
                        return ctx.done();
                    } else if (cmd.getInitialAmount() < 1) {
                        ctx.invalidCommand("Should at least add one");
                        return ctx.done();
                    } else {
                        return ctx.thenPersist(
                                ShoppingBasketEvent.ItemAddedToShoppingBasket.builder()
                                        .shoppingBasketId(state().getShoppingBasket().get().getId())
                                        .shopId(state().getShoppingBasket().get().getShopId())
                                        .customerId(state().getShoppingBasket().get().getCustomerId())
                                        .skuId(cmd.getSkuId())
                                        .initialAmount(cmd.getInitialAmount())
                                        .build(),
                                evt -> ctx.reply(Done.getInstance()));
                    }
                }
        );

        b.setEventHandler(ShoppingBasketEvent.ItemAddedToShoppingBasket.class,
                evt -> state().withShoppingBasket(
                        Optional.of(
                                ShoppingBasketLogic.addItemToShoppingBasket(
                                        state().getShoppingBasket().get(),
                                        evt.getSkuId(),
                                        evt.getInitialAmount())
                        )
                )
        );
    }

    private void addBehaviorItemAmountUpdatedInShoppingBasket(final BehaviorBuilder b) {
        b.setCommandHandler(ShoppingBasketCommand.UpdateItemAmountInShoppingBasket.class,
                (cmd, ctx) -> {
                    OptionalInt existingAmount = ShoppingBasketLogic.getAmountForSku(state().getShoppingBasket().get(), cmd.getSkuId());
                    if (!state().getShoppingBasket().isPresent()) {
                        ctx.invalidCommand("Shoppingbasket hasn't been created yet");
                        return ctx.done();
                    } else if (!existingAmount.isPresent()) {
                        ctx.invalidCommand("Item has not yet been added, please add it first");
                        return ctx.done();
                    } else if (cmd.getNewAmount() < 1) {
                        ctx.invalidCommand("Should at least have one");
                        return ctx.done();
                    } else if (existingAmount.getAsInt() == cmd.getNewAmount()) {
                        ctx.invalidCommand("Item already has given amount");
                        return ctx.done();
                    } else {
                        return ctx.thenPersist(
                                ShoppingBasketEvent.ItemAmountUpdatedInShoppingBasket.builder()
                                        .shoppingBasketId(state().getShoppingBasket().get().getId())
                                        .skuId(cmd.getSkuId())
                                        .newAmount(cmd.getNewAmount())
                                        .build(),
                                evt -> ctx.reply(Done.getInstance()));
                    }
                }
        );

        b.setEventHandler(ShoppingBasketEvent.ItemAmountUpdatedInShoppingBasket.class,
                evt -> state().withShoppingBasket(
                        Optional.of(
                                ShoppingBasketLogic.addItemToShoppingBasket(
                                        state().getShoppingBasket().get(),
                                        evt.getSkuId(),
                                        evt.getNewAmount())
                        )
                )
        );
    }

    private void addBehaviorForRemoveItemFromShoppingBasket(final BehaviorBuilder b) {
        b.setCommandHandler(ShoppingBasketCommand.RemoveItemFromShoppingBasket.class,
                (cmd, ctx) -> {
                    if (!state().getShoppingBasket().isPresent()) {
                        ctx.invalidCommand("Shoppingbasket hasn't been created yet");
                        return ctx.done();
                    } else {
                        return ctx.thenPersist(
                                ShoppingBasketEvent.ItemRemovedFromShoppingBasket.builder()
                                        .shoppingBasketId(state().getShoppingBasket().get().getId())
                                        .skuId(cmd.getSkuId())
                                        .build(),
                                evt -> ctx.reply(Done.getInstance()));
                    }
                });

        b.setEventHandler(ShoppingBasketEvent.ItemRemovedFromShoppingBasket.class,
                evt -> state().withShoppingBasket(
                        Optional.of(
                                ShoppingBasketLogic.removeItemFromShoppingBasket(
                                        state().getShoppingBasket().get(),
                                        evt.getSkuId())
                        )
                )
        );
    }
}
