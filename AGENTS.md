# Thinbus SRP JavaScript Agent Guide

This repository contains a JavaScript implementation of the [Secure Remote Password (SRP-6a)](http://srp.stanford.edu/) protocol, along with compatible Java classes. SRP allows for zero-knowledge proof-of-password authentication between a client and server.

## Repository Overview

The repository provides implementations of both client and server roles in the SRP-6a protocol in both JavaScript and Java:

- **JavaScript SRP Client**: Implementation of the SRP-6a client role, commonly used in web browsers
- **JavaScript SRP Server**: Implementation of the SRP-6a server role, can be used in Node.js environments
- **Java SRP Client**: Implementation of the SRP-6a client role in Java
- **Java SRP Server**: Implementation of the SRP-6a server role in Java using Nimbus SRP6a library
- **Utilities**: Tools for generating safe primes and other cryptographic parameters
- **Tests**: Comprehensive test suite for all implementations

## Architecture Overview

### Core Components

1. **JavaScript Implementations** (`src/main/resources/js/`)
   - **SRP Client Role**:
     - `thinbus-srp6client.js` - Base client implementation
     - `thinbus-srp6client-sha256.js` - SHA-256 client implementation (recommended)
     - `thinbus-srp6client-sha1.js` - SHA-1 client implementation (legacy)
   - **SRP Server Role**:
     - `thinbus-srp6server.js` - Base server implementation
     - `thinbus-srp6server-sha256.js` - SHA-256 server implementation (recommended)
     - `thinbus-srp6server-sha1.js` - SHA-1 server implementation (legacy)
   - **Supporting Libraries**:
     - `sha1.js` / `sha256.js` - Hash implementations
     - `isaac.js` - PRNG fallback when WebCryptoAPI unavailable
     - `random.js` - Secure random number generation wrapper
     - `biginteger.js` - Big integer math library

2. **Java Implementations** (`src/main/java/com/bitbucket/thinbus/srp6/js/`)
   - **SRP Server Role**:
     - `SRP6JavascriptServerSessionSHA256` - SHA-256 server implementation (recommended)
     - `SRP6JavascriptServerSessionSHA1` - SHA-1 server implementation (legacy)
   - **SRP Client Role**:
     - `SRP6JavaClientSessionSHA256` - SHA-256 client implementation
     - `SRP6JavaClientSessionSHA1` - SHA-1 client implementation
   - **Utilities**:
     - `HexHashedVerifierGenerator` - Creates salt and verifier from password

### Key Design Patterns

- **Zero-Knowledge Proof**: The SRP protocol allows a client to prove knowledge of a password without sending it
- **Client-Server Protocol Roles**: 
  - In the SRP protocol, there are two distinct roles: "client" and "server"
  - These roles are independent of where the code is deployed (browser, Node.js server, Java application, etc.)
  - A JavaScript application running in Node.js can act as either an SRP client or an SRP server
  - A Java application can act as either an SRP client or an SRP server
  - Browser JavaScript is typically limited to the SRP client role due to browser sandbox restrictions
- **Session-Based**: Each authentication creates a new session with fresh ephemeral keys
- **Multi-Hash Support**: SHA-256 (default) and SHA-1 (legacy)
- **No External Dependencies**: JavaScript is self-contained for easy deployment

## Setup Commands

- **Basic build and test** (requires OpenJDK 11): `mvn -Pjdk11 test package`
- **Run tests with relaxed warnings**: `mvn test -P relaxed`
- **Run specific test class**: `mvn -Pjdk11 test -Dtest=TestClassName`
- **Generate JAR with dependencies**: `mvn assembly:assembly`
- **Deploy snapshot to Sonatype**: `mvn clean deploy`
- **Full build with strict compiler warnings**: `mvn clean package`
- **Generate custom parameters**: `java -jar target/thinbus-srp6a-js-<version>-jar-with-dependencies.jar /path/to/dhparam.txt <hash>`

## Development Workflow

1. **Understanding the Protocol**: Before making changes, ensure you understand the SRP protocol as defined at [srp.stanford.edu/design.html](http://srp.stanford.edu/design.html)
2. **JavaScript Changes**: 
   - Source files are in `src/main/resources/js/`
   - Modify the unminified versions first
   - JavaScript files are automatically minified during build
   - JSHint runs automatically during build for validation
   - Run tests to verify changes
3. **Java Changes**:
   - Source files are in `src/main/java/com/bitbucket/thinbus/srp6/js/`
   - Ensure compatibility with JavaScript client
   - Requires OpenJDK 11 for building/testing
   - Use `-P relaxed` profile during development, but fix all warnings before committing

## Testing Instructions

- Run all tests: `mvn -Pjdk11 test`
- Run specific test: `mvn -Pjdk11 test -Dtest=TestClassName`
- Run tests with relaxed warnings: `mvn test -P relaxed`
- JavaScript tests are executed via GraalVM integration that runs JavaScript code directly
- Test files location:
  - Java tests: `src/test/java/`
  - JavaScript tests: `src/test/resources/`
- Key test classes:
  - `TestSRP6JavascriptClientSessionSHA256.java`: Tests the JavaScript SRP client implementation with SHA-256
  - `TestSRP6JavascriptServerSessionSHA256.java`: Tests the JavaScript SRP server implementation with SHA-256
  - `TestJavaClient.java`: Tests the Java SRP client implementation
  - `TestSRP6JavascriptClientSessionSHA1.java`: Tests the JavaScript SRP client implementation with SHA-1
  - `TestSRP6JavascriptServerSessionSHA1.java`: Tests the JavaScript SRP server implementation with SHA-1
- Cross-role testing:
  - Tests verify that a JavaScript SRP client can authenticate with a Java SRP server
  - Tests verify that a Java SRP client can authenticate with a JavaScript SRP server
  - Tests verify that a JavaScript SRP client can authenticate with a JavaScript SRP server
  - Tests verify that a Java SRP client can authenticate with a Java SRP server

## Code Style and Conventions

- **Java**:
  - Standard Java conventions
  - Comprehensive JavaDoc comments
  - Immutable objects where possible
- **JavaScript**:
  - Follows standard JavaScript conventions
  - Minified versions are generated from source
  - Avoid modifying global scope

## Security Considerations

- **Random Numbers**: The code attempts to use WebCryptoAPI for secure random numbers, falling back to the ISAAC PRNG
- **Salt Generation**: Salts should be unique per user, enforced with database constraints
- **Verifier Storage**: Verifiers should be encrypted in the database
- **Protocol Integrity**: Do not modify the protocol flow without understanding the security implications
- **Testing**: Always test security-critical changes thoroughly

## Common Tasks

### 1. Repository Management

This project is maintained in two repositories that are kept in sync:

- **Original Upstream Repository**: [Bitbucket](https://bitbucket.org/simon_massey/thinbus-srp-js)
- **Mirror Repository**: [GitHub](https://github.com/simbo1905/thinbus-srp-js)

When contributing to this project, be aware that changes need to be synchronized between both repositories. The typical workflow involves:

1. Making changes in one repository
2. Pushing those changes to the other repository to maintain synchronization

#### GitHub Workflow

The GitHub CLI (`gh`) is available for managing issues and pull requests on the GitHub repository:

```bash
# Create a new issue
gh issue create --title "Issue title" --body "Issue description"

# Create a pull request
gh pr create --title "PR title" --body "PR description"

# List open issues
gh issue list

# Check out a PR for review
gh pr checkout <pr-number>

# Review a PR
gh pr review <pr-number> --approve
```

#### Repository Synchronization

To keep both repositories in sync:

```bash
# Add both remotes (if not already configured)
git remote add origin git@bitbucket.org:simon_massey/thinbus-srp-js.git
git remote add github git@github.com:simbo1905/thinbus-srp-js.git

# Push changes to both repositories
git push origin <branch-name>
git push github <branch-name>

# Pull changes from Bitbucket (primary)
git pull origin <branch-name>
```

### 2. Generating Custom Safe Primes

```bash
# Create parameters with openssl
openssl dhparam -text <bit-length> | tee /tmp/my_dhparam.txt

# Build the runnable jar
mvn assembly:assembly

# Generate configuration
java -jar target/thinbus-srp6a-js-<version>-jar-with-dependencies.jar /tmp/my_dhparam.txt SHA-256
```

### 3. Understanding SRP Client and Server Roles

The SRP protocol defines two distinct roles:

1. **SRP Client Role**:
   - Generates verifier during registration
   - Proves knowledge of password during authentication
   - Can be implemented in JavaScript (browser or Node.js) or Java

2. **SRP Server Role**:
   - Stores salt and verifier (not the password)
   - Verifies client's proof during authentication
   - Can be implemented in JavaScript (Node.js) or Java

These roles are independent of deployment environment:
- A Java application can act as either an SRP client or server
- A Node.js application can act as either an SRP client or server
- A browser application is typically limited to the SRP client role

### 4. Implementing SRP Authentication Flow

See the `Demo.java` file for a complete example of:
- Client registration (generating salt and verifier)
- Client authentication (proof of password)
- Server verification
- Session key generation for follow-on cryptography

### 5. Integrating with Web Applications

- For Spring applications, see the [thinbus-srp-spring-demo](https://bitbucket.org/simon_massey/thinbus-srp-spring-demo/overview)
- For PHP applications, see [thinbus-php](https://bitbucket.org/simon_massey/thinbus-php/overview)
- For Python applications, see [pysrp_thinbus](https://github.com/SthPhoenix/pysrp_thinbus)

## Troubleshooting

- **Browser Compatibility**: Tested on IE8+, Edge, Chrome, Firefox, and Safari
- **Random Number Generation**: Check `random16byteHex.isWebCryptoAPI()` to verify secure random availability
- **Performance Issues**: Smaller prime sizes (1024 bit) may be needed for low-spec devices
- **Protocol Errors**: Ensure all values are being passed correctly between client and server

## Maven Dependency

```xml
<dependency>
    <groupId>org.bitbucket.simon_massey</groupId>
    <artifactId>thinbus-srp6a-js</artifactId>
    <version>1.6.2</version>
</dependency>
```