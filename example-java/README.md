# Example Java

## Resolve a [`TDW`](https://identity.foundation/didwebvh/v0.3) DID document

This project contains examples for different approaches to resolve a [`TDW`](https://identity.foundation/didwebvh/v0.3) DID document:
- [User provided HTTP client](/example-java/src/main/java/org/examples/ExampleWithHttpClient.java) -> uses an HTTP client to fetch a DID log and then resolves it using the library

## Project integration

Accommodate the `pom.xml` file to be able to import the required dependencies.
For current/latest versions of the required dependencies, just rely on the relevant 
[`didresolver` Maven Central Repository packages](https://repo1.maven.org/maven2/io/github/swiyu-admin-ch/didresolver) 
(as well as [`didtoolbox` Maven Central Repository packages](https://repo1.maven.org/maven2/io/github/swiyu-admin-ch/didtoolbox), 
used here though only for testing purposes only)
```xml
<!-- file: pom.xml -->
<dependencies>

    <dependency>
        <groupId>io.github.swiyu-admin-ch</groupId>
        <artifactId>didresolver</artifactId>
        <!--version>[ANY_AVAILABLE_VERSION]</version-->
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
        <groupId>io.github.swiyu-admin-ch</groupId>
        <artifactId>didtoolbox</artifactId>
        <!--version>[ANY_AVAILABLE_VERSION]</version-->
    </dependency>

    <!-- Other dependencies go here -->

</dependencies>
```

Needles to say, before running the `mvn clean install -U` command, it is assumed that you are already familiar with [Maven](https://docs.github.com/articles/configuring-apache-maven-for-use-with-github-package-registry).
