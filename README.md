A small proof of concept of a shopping basket service that I made to become more familiar with [Lagom] (https://www.lightbend.com/lagom).

## Notes

While working on this PoC I found the following documentation/code very helpful:
- [Lagom reference manual](http://www.lagomframework.com/documentation/1.2.x/java/Home.html)
- [The Lagom Framework for reactive microservices] (http://musigma.org/java/2016/11/14/lagom.html) by Matt Sicker
- [Lagom's Java chirper template] (https://github.com/lagom/activator-lagom-java-chirper)

#### Initial setup
The project was initialized using the Maven archetype:
```bash
mvn archetype:generate -DarchetypeGroupId=com.lightbend.lagom \
  -DarchetypeArtifactId=maven-archetype-lagom-java -DarchetypeVersion=1.2.0
```

#### Lombok
Lombok is being used to automatically generate immutable PoJo's.
In IntelliJ you need to install a [plugin] (https://plugins.jetbrains.com/idea/plugin/6317-lombok-plugin) to make this work.
Lightbend advises to make use of the Immutables library. But this currently requires some additional configuration steps in IntelliJ, which I found too cumbersome. 

#### Running
The project can be started with:
```bash
mvn lagom:runAll
```

#### Keeping state
In this implementation the [`ShoppingBasketEntity`] (../master/shoppingbasket-impl/src/main/java/nl/brightbits/lagom/shoppingbasket/impl/ShoppingBasketEntity.java)
uses the [`ShoppingBasket`]  (../master/shoppingbasket-api/src/main/java/nl/brightbits/lagom/shoppingbasket/api/ShoppingBasket.java) read model to keep its state.
But in a more serious system it is advisable to not use a read model to keep state. Creating a separate state model allows for more flexibility when it comes to being able to update the read model (which is what CQRS is all about of course :smile:).

## Functionality

It's possible to interact with the shopping basket service using the following commands:

Create a new shopping basket
```bash
curl -X POST 'http://localhost:9000/api/shoppingbasket' \
 -d '{"shopId": "1", "customerId": "1"}'
```
_This returns the uuid of the shopping basket_

Get the shopping basket
```bash
curl -X GET 'http://localhost:9000/api/shoppingbasket/<UUID>'
```

Get the most recent shopping basket of a customer
```bash
curl -v -X GET 'http://localhost:9000/api/shoppingbasket/mostRecent' \
 -d '{"shopId": "1", "customerId": "1"}'
```
_This operation relies only on the read-side, requesting the required data from a Cassandra table instead of from the state of a `PersistentEntity`_

Add an item to the shopping basket
```bash
curl -X POST 'http://localhost:9000/api/shoppingbasket/<UUID>/items' \
 -d '{"skuId": "abc123", "initialAmount": "1"}'
```

Update the amount of an item in the shopping basket
```bash
curl -X PUT 'http://localhost:9000/api/shoppingbasket/<UUID>/items' \
 -d '{"skuId": "abc123", "newAmount": "2"}'
```

Delete an item from the shopping basket
```bash
curl -X DELETE 'http://localhost:9000/api/shoppingbasket/<UUID>/items' \
 -d '{"skuId": "abc123"}'
```
