# service-stub

Primitive mock service

## Usage

```
clojure -A:dev -e  "(start)"
```

## Mock URIs

/receipt-services/order - returns a canned receipt json response (currenly a constant, regardless of an query params.)

See service_stub/main to add additional mock responses.
