# Java SDK TCK Server

This module contains implementation of the [SDK TCK specification](https://github.com/hashgraph/hedera-sdk-tck/) for the Java SDK TCK.

## Features

- JSON-RPC Server: Implements the JSON-RPC protocol for communication over the HTTP protocol.
- Compliance: Follows the SDK TCK specification for Java SDK.

## Functionality

The Java SDK JSON-RPC server parses JSON-formatted requests received from the test driver. Upon receiving a request, it executes the corresponding function or procedure associated with the method specified in the request. Subsequently, it prepares the response in JSON format and sends it back to the test driver.

## Setup and starting the server

1. Chdir into /tck.
```shell
  cd tck
```
2. Build the project using Gradle.
```shell
  ./gradlew build
```
3. Run the server.
```shell
  ./gradlew bootRun
```

By default, the server will occupy port 80. If you need to specify a different port, modify the port in the `application.yml` file:
``` yaml
server:
    port: <PORT>
```
