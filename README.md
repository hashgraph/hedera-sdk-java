# Build status

Version 0.1.0

[![CircleCI](https://circleci.com/gh/swirlds/hedera-sdk-java.svg?style=shield&circle-token=d288d5d093d2529ad8fbd5d2e8b3e26be22dcaf7)](https://circleci.com/gh/swirlds/hedera-sdk-java)

# Java version

This project was developed with Java version 10.0.2 (java -version returns build 10.0.2+13).

# Project install in Eclipse

this project is built with Maven, import into eclipse as a Maven project

# Prerequisites

## Right click on pom.xml and choose Run As->Maven Install 

Note that the .proto files are compiled with Maven, so the project may initially look full of errors, this is normal.
If there are still some project issues, try a Maven project update and project clean followed by a Maven install.

## Javadocs 

Javadocs are generated automatically as part of the maven build (if run from Eclipse, make sure your JAVA HOME is set otherwise the build will fail).
They are generated as a JAR file which is compiled into the target/ folder.

## Running the examples

A node.properties.sample is provided, copy the file to node.properties and update with your account details, the details of the node you want to communicate to and finally, your private and public keys (as hex strings).
This file is ignored by git so all changes will remain local.

