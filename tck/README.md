# Java SDK TCK Server

## Description

This module contains implementation of the JSON-RPC server for the Java SDK to interpret and process requests from the Test Driver based on the [TCK's](https://github.com/hiero-ledger/hiero-sdk-tck) requirements. Upon receiving a request, it executes the corresponding function or procedure associated with the method specified in the request. Subsequently, it prepares the response in JSON format and sends it back to the test driver.

## Setup

**1. Navigate into tck directory.**

```shell
cd tck
```

**2. Build the project using Gradle.**

```shell
../gradlew build
```

**3. Run the server.**

```shell
../gradlew bootRun
```

By default, the server will occupy port 80. If you need to specify a different port, modify the port in the `application.yml` file:

```yaml
server:
    port: <PORT>
```
