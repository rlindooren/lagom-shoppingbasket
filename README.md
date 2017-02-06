A small proof of concept that I made to get familiar with [Lagom] (https://www.lightbend.com/lagom).

While working on this PoC I found the following documentation/code very helpful:
- [Lagom reference manual](http://www.lagomframework.com/documentation/1.2.x/java/Home.html)
- [The Lagom Framework for reactive microservices] (http://musigma.org/java/2016/11/14/lagom.html) by Matt Sicker
- [Lagom's Java chirper template] (https://github.com/lagom/activator-lagom-java-chirper)

The project was initialized using the Maven archetype:
```bash
$ mvn archetype:generate -DarchetypeGroupId=com.lightbend.lagom \
  -DarchetypeArtifactId=maven-archetype-lagom-java -DarchetypeVersion=1.2.0
```

The project can be started with:
```bash
mvn lagom:runAll
```

It's possible to interact with the shopping basket service using the following commands:

Create a new shopping basket
```bash
curl -X POST 'http://localhost:9000/api/shoppingbasket' -d '{"shopId": "1", "customerId": "1"}'
```
_This returns the uuid of the shopping basket_

Get the shopping basket
```bash
curl -X GET 'http://localhost:9000/api/shoppingbasket/<UUID>'
```

Add an item to the shopping basket
```bash
curl -X POST 'http://localhost:9000/api/shoppingbasket/<UUID>/items' -d '{"skuId": "abc123", "initialAmount": "1"}'
```

Update the amount of an item in the shopping basket
```bash
curl -X PUT 'http://localhost:9000/api/shoppingbasket/<UUID>/items' -d '{"skuId": "abc123", "newAmount": "2"}'
```

Delete an item from the shopping basket
```bash
curl -X DELETE 'http://localhost:9000/api/shoppingbasket/<UUID>/items' -d '{"skuId": "abc123"}'
```
