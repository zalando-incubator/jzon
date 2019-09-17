# Jzon
A set of APIs for finding keys in different structures in a json.

The library is using behind the scenes the [JsonPath](https://github.com/json-path/JsonPath/) for parsing the Json payloads.
JsonPath is a powefull library, however it is limited when it comes to ease of use for doing complex, not straighforward parsing. The Jzon library provides multiple ways to retrieve the specific values for the provided keys from the payload in an easier way without the need to deal with the [Operators](https://github.com/json-path/JsonPath/#operators) defined in the JsonPath library both for simple and complex key/value lookups: 

## Installation

In oder to use it the following dependency should be added to the project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>jzon</artifactId>
    <version>1.4.0</version>
</dependency
```
## Usage

In order to use the library you need an instance of the interfaces which can be instantiated using the `PayloadKeyFactory` class as following:

```java
PayloadKeyFactory payloadKeyFactory = new PayloadKeyFactory();
PayloadKeyParser payloadKeyParser = payloadKeyFactory.getPayloadKeyParser(objectMapper);
```
### Example 1: 
With JsonPath finding the values for `customer_number` is easy and can be done in one line: 

```java 
JsonPath.parse(jsonPayload).read("$.." + "customerNumber")
```
however for the following payloads provided the same `customer_number` key things become more complex quite fast and cannot be done in one line: 

```json
//With split key
{
  "size": 27,
  "customer": {
    "number": "3"
  }
}
```
OR 

```json
//With split key and multiple occurrence 
{
  "size": 27,
  "customer": [
    {
      "number": "3"
    },
    {
      "number": "4"
    }
  ]
}
```
but using the Jzon it is still easy: 

```java
final Map<String, Object[]> knownKeyValues = payloadKeyParser.parse(newHashSet("customer_number"), Collections.emptyMap(), jsonPayload, AS_VALUE_LIST);
```
The library still supports the following easier scenarios with the same command as above, providing a unified API call for all the use cases. And it resolves all the values of `[1,2,3,4,5]` for the `customer_number`, regardless of level in which the key is residing in the hierarchy or the structure of the json. The `jsonPayload` can be both of type `String` or `Map<String,Object>`.

```json
//At root level
{
  "size": 27,
  "customer_number": "1"
}
```
OR

```json
//Not at root
{
  "size": 27,
  "customer": {
    "customer_number": "2"
  }
}
```
OR

```json
// Multiple values
{
  "size": 27,
  "customers": [
    {
      "customer_number": "4"
    },
    {
      "customer_number": "5"
    }
  ]
}
```

### Example 2: 
Consider we want to find the `customer_number` in the following payload alongside with the other customer related information such as `customer_email` or `customer_hash`. Using the JsonPath it is not possible to do this with one API call and you cannot know which `customer_hash` belongs to which `customer_number`, whereas with Jzon you can use the `parseGrouped` method to find and retrieve information at the same level in a json belonging to the same object using a `groupKey` like this: 

```java
final List<Map<String, Object>> resultMap = payloadKeyParser.parseGrouped("customer_number", newHashSet("customer_address", "customer_hash"), emptySet(), jsonPayload);
```

```json
//Parsing in groups
{
  "customers": [
    {
      "customer_number": 12,
      "customer_email": "test1@email.com",
      "customer_address": "dummyAddress1",
      "customer_hash": "12anyStupidHash"
    },
    {
      "customer_number": 23,
      "customer_email": "test2@email.com",
      "customer_address": "dummyAddress2",
      "customer_hash": "45anyStupidHash"
    },
    {
      "customer_number": 34,
      "customer_email": "test3@email.com",
      "customer_address": "dummyAddress3"
    }
  ]
}
```

### Replacement Keys: 

If the payload contains `customerNumber` instead and you don't want to change your keys (e.g. hard-coded keys) you can provide a set of replacement keys (e.g. fetched from a configuration from outside or another service)
and then provide it as following: 

```java  
Map<String, String> replacementKeys = new HashMap<>();
replacementKeys.put("customer_number", "customerNumber");
final Map<String, Object[]> knownKeyValues = payloadKeyParser.parse(newHashSet("customer_number"), replacementKeys, payload, AS_VALUE_LIST);
```
   
The library also gives the possibility to find out the path of the provided keys in the payload.

```java
final Map<String, Object[]> knownKeyValues = payloadKeyParser.parse(newHashSet("customer_number"), Collections.emptyMap(), payload, AS_PATH_LIST);
```
In case of the above examples if we look up for the paths the following values are returned:

```java
$['cutomer_number']
$[customer]['cutomer_number']
$[customer]['number']
$[customers][0]['customer_number']
$[customers][1]['customer_number']
```
    
For more information about the other APIs look into the JavaDocs of the classes and interface under `org.zalando.jzon.service`.

The package `org.zalando.jzon.service.util` also provides a set of utility methods in order to process the responses of the APIs easier. 

The unit tests of the library provide an extensive amount examples and scenarios on how to use the library.

## Authors

- **[Sina Golesorkhi](https://github.com/sina-golesorkhi)**

  
- **[Valentina Georgieva](https://github.com/vkoteva)**


