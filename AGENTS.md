# Thinbus SRP JavaScript Agent Guide

This repository contains a JavaScript implementation of the [Secure Remote Password (SRP-6a)](http://srp.stanford.edu/) protocol for web browsers, along with compatible Java server-side classes. SRP allows for zero-knowledge proof-of-password authentication between a client and server.

## Repository Overview

- **JavaScript Client**: Browser-side implementation of SRP-6a
- **Java Server**: Compatible server-side implementation using Nimbus SRP6a Java library
- **Utilities**: Tools for generating safe primes and other cryptographic parameters
- **Tests**: Comprehensive test suite for both JavaScript and Java components

## Architecture Overview

### Core Components

1. **JavaScript Client** (`src/main/resources/js/`)
   - `thinbus-srp6client.js` - Main client implementation
   - `sha1.js` / `sha256.js` - Hash implementations
   - `isaac.js` - PRNG fallback when WebCryptoAPI unavailable
   - `random.js` - Secure random number generation wrapper

2. **Java Server** (`src/main/java/com/bitbucket/thinbus/srp6/js/`)
   - `SRP6JavascriptServerSessionSHA256` - Main server session (recommended)
   - `SRP6JavascriptServerSessionSHA1` - Legacy SHA-1 support
   - `SRP6JavaClientSession` - For Java-to-Java authentication
   - `HexHashedVerifierGenerator` - Creates salt and verifier from password

### Key Design Patterns

- **Zero-Knowledge Proof**: Client proves password knowledge without sending it
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
  - JavaScript tests: `src/test/javascript/`
- Key test classes:
  - `TestSRP6JavascriptClientSessionSHA256.java`: Tests the SHA-256 JavaScript client
  - `TestSRP6JavascriptServerSessionSHA256.java`: Tests the SHA-256 server implementation
  - `TestJavaClient.java`: Tests the Java client implementation
- Cross-platform testing: Verify changes work in both Java and JavaScript contexts

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

### 1. GitHub Workflow

This project uses GitHub for source control. The GitHub CLI (`gh`) is available for managing issues and pull requests:

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

### 2. Generating Custom Safe Primes

```bash
# Create parameters with openssl
openssl dhparam -text <bit-length> | tee /tmp/my_dhparam.txt

# Build the runnable jar
mvn assembly:assembly

# Generate configuration
java -jar target/thinbus-srp6a-js-<version>-jar-with-dependencies.jar /tmp/my_dhparam.txt SHA-256
```

### 3. Implementing SRP Authentication Flow

See the `Demo.java` file for a complete example of:
- Client registration (generating salt and verifier)
- Client authentication (proof of password)
- Server verification
- Session key generation for follow-on cryptography

### 4. Integrating with Web Applications

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