# Example Java

## Resolve a [`TDW`](https://identity.foundation/didwebvh/v0.3) DID document

This project contains examples for different approaches to resolve a [`TDW`](https://identity.foundation/didwebvh/v0.3) DID document:
- [User provided HTTP client](/src/main/java/org/examples/ExampleWithHttpClient.java) -> uses an HTTP client to fetch a DID log and then resolves it using the library

## Project integration

Accommodate the `pom.xml` file to be able to import the required dependencies.
For current/latest versions of the required dependencies, just rely on the relevant [`didresolver` GitHub packages](https://github.com/swiyu-admin-ch/didresolver-kotlin/packages/2414675) (as well as [`didtoolbox` GitHub packages](https://github.com/swiyu-admin-ch/didtoolbox-java/packages/2420331), used here though only for testing purposes only)
```xml
<!-- file: pom.xml -->
<dependencies>

    <dependency>
        <groupId>ch.admin.eid</groupId>
        <artifactId>didresolver</artifactId>
        <version>{latest version as seen in repository}</version>
    </dependency>

    <!-- MANDATORY (required by didresolver) -->
    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna</artifactId>
        <version>5.14.0</version>
    </dependency>

    <!-- optional, for JSON manipulation -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
    <!-- optional, used here for testing purposes only -->
    <dependency>
        <groupId>ch.admin.bj.swiyu</groupId>
        <artifactId>didtoolbox</artifactId>
        <version>{latest version as seen in repository}</version>
    </dependency>

    <!-- Other dependencies go here -->

</dependencies>
```

Needles to say, before running the `mvn clean install -U` command, it is assumed that you are already familiar with [Maven](https://docs.github.com/articles/configuring-apache-maven-for-use-with-github-package-registry).
