# Scala Examples

The project is split into two modules

1. Json Parser

Uses `Jackson` to deserialize a `java.util.InputStream` to `JsonNode` and from there 
a mix of `pattern matching` and `recursion` is used to convert to a `Map[String, Any]`.

2. Exchange rates API

Uses `api.exchangeratesapi.io` to retrieve the latest exchange rates 
and convert amounts from one currency to another. 
Wrote `unit tests` where I mock the external API request and also 
wrote `integration tests` that actually do requests to the external API.

# Run

To build and run the tests do:
``` 
./gradlew 
```

# Technologies used

- Scala 2.12
- Akka Http with Spray json
- Jackson
- Scalatest 
