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
        if (snapshotState.isPresent() && snapshotState.get().getShoppingBasket().isPresent()) {
            return shoppingBasketCreatedBehavior(snapshotState.get());
        } else {
            return shoppingBasketNotCreatedBehavior();
        }
    }

    private Behavior shoppingBasketNotCreatedBehavior() {
        final BehaviorBuilder b = newBehaviorBuilder(
                ShoppingBasketState.builder()
                        .shoppingBasket(Optional.empty())
                        .build());

        b.setCommandHandler(ShoppingBasketCommand.CreateShoppingBasket.class,
                (cmd, ctx) -> ctx.thenPersist(
                        ShoppingBasketEvent.ShoppingBasketCreated.builder()
                                .id(entityId())
                                .shopId(cmd.getShopId())
                                .customerId(cmd.getCustomerId())
                                .build(),
                        evt -> ctx.reply(entityId()))
        );

        b.setEventHandlerChangingBehavior(ShoppingBasketEvent.ShoppingBasketCreated.class,
                evt -> shoppingBasketCreatedBehavior(new ShoppingBasketState(
                        Optional.of(
                                ShoppingBasket.builder()
                                        .id(evt.getId())
                                        .shopId(evt.getShopId())
                                        .customerId(evt.getCustomerId())
                                        .items(Optional.empty())
                                        .build()
                        ))));

        b.setReadOnlyCommandHandler(ShoppingBasketCommand.GetShoppingBasket.class,
                (cmd, ctx) -> ctx.invalidCommand("Shopping basket hasn't been created yet"));

        b.setReadOnlyCommandHandler(ShoppingBasketCommand.AddItemInShoppingBasket.class,
                (cmd, ctx) -> ctx.invalidCommand("Shopping basket hasn't been created yet"));

        b.setReadOnlyCommandHandler(ShoppingBasketCommand.UpdateItemAmountInShoppingBasket.class,
                (cmd, ctx) -> ctx.invalidCommand("Shopping basket hasn't been created yet"));

        b.setReadOnlyCommandHandler(ShoppingBasketCommand.RemoveItemFromShoppingBasket.class,
                (cmd, ctx) -> ctx.invalidCommand("Shopping basket hasn't been created yet"));

        return b.build();
    }

    private Behavior shoppingBasketCreatedBehavior(ShoppingBasketState state) {
        final BehaviorBuilder b = newBehaviorBuilder(state);

        b.setReadOnlyCommandHandler(ShoppingBasketCommand.CreateShoppingBasket.class,
                (cmd, ctx) -> ctx.invalidCommand("Shopping basket " + entityId() + " has already been created"));

        b.setReadOnlyCommandHandler(ShoppingBasketCommand.GetShoppingBasket.class,
                (cmd, ctx) -> ctx.reply(state().getShoppingBasket().get()));

        addAddItemInShoppingBasketBehavior(b);
        addUpdateItemAmountInShoppingBasketBehavior(b);
        addRemoveItemFromShoppingBasketBehavior(b);

        return b.build();
    }

    private void addAddItemInShoppingBasketBehavior(BehaviorBuilder b) {
        b.setCommandHandler(ShoppingBasketCommand.AddItemInShoppingBasket.class,
                (cmd, ctx) -> {
                    if (ShoppingBasketLogic.getAmountForSku(state().getShoppingBasket().get(), cmd.getSkuId())
                            .isPresent()) {
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

    private void addUpdateItemAmountInShoppingBasketBehavior(final BehaviorBuilder b) {
        b.setCommandHandler(ShoppingBasketCommand.UpdateItemAmountInShoppingBasket.class,
                (cmd, ctx) -> {
                    OptionalInt existingAmount = ShoppingBasketLogic.getAmountForSku(state().getShoppingBasket().get(), cmd.getSkuId());
                    if (!existingAmount.isPresent()) {
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

    private void addRemoveItemFromShoppingBasketBehavior(final BehaviorBuilder b) {
        b.setCommandHandler(ShoppingBasketCommand.RemoveItemFromShoppingBasket.class,
                (cmd, ctx) -> ctx.thenPersist(
                        ShoppingBasketEvent.ItemRemovedFromShoppingBasket.builder()
                                .shoppingBasketId(state().getShoppingBasket().get().getId())
                                .skuId(cmd.getSkuId())
                                .build(),
                        evt -> ctx.reply(Done.getInstance()))
        );

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
