package nl.brightbits.lagom.shoppingbasket.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import nl.brightbits.lagom.shoppingbasket.api.ShoppingBasketService;

public class ShoppingBasketModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(serviceBinding(ShoppingBasketService.class, ShoppingBasketServiceImpl.class));
    }
}
