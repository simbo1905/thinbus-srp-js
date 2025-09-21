# Thinbus SRP Java Implementation Agent Guide

This repository contains a standalone Java implementation of the [Secure Remote Password (SRP-6a)](http://srp.stanford.edu/) protocol using the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. It is designed to be compatible with the [thinbus-srp npm package](https://github.com/simbo1905/thinbus-srp-npm) for cross-platform interoperability.

## Software Engineering Practices

### Issue/Commit/PR Guidelines

**Issues** should only state WHAT and WHY:
- What functionality is needed or broken
- Why it's important for the project
- NO solution approaches, implementation details, or technical specifics
- Focus on business requirements and user impact

**Commits** should state WHAT was achieved and HOW to test:
- What specific changes were made
- How to verify the changes work (test commands, expected results)
- Technical implementation details
- DO NOT repeat issue content
- Include Co-authored-by for AI assistance

**Pull Requests** should show the work and discuss caveats:
- Link to the issue being addressed
- Highlight important implementation decisions
- Discuss any trade-offs or limitations
- Request specific feedback on technical approaches
- DO NOT repeat commit messages or issue descriptions
- When merged, should reference the issue closed and how to use the feature

**CRITICAL: Zero Broken Tests Policy**
- NEVER create a PR with failing tests
- All PRs must be marked as DRAFT until 100% of tests pass
- CI must verify exact test counts to prevent silent failures
- Any test failures must be fixed before marking PR as ready for review

## Prerequisites

**Java 21 or higher is required** for building and running this project. Before starting any work, verify your Java version:

```bash
java --version
```

The output should show Java 21 or higher. If you don't have Java 21+, you'll need to install it before proceeding.

**GitHub CLI (gh) is required** for issue and PR management. Install and authenticate:

```bash
# Install gh CLI and authenticate
gh auth login
gh --help  # Verify installation
```

## Repository Overview

This repository provides a standalone Java SRP6a implementation that can interoperate with JavaScript implementations across different platforms:

### Java Implementation (Primary Focus)
- **Java SRP Client**: RFC5054-compliant SRP client implementation using Nimbus cryptographic library
- **Java SRP Server**: RFC5054-compliant SRP server implementation using Nimbus cryptographic library
- **Validates RFC5054**: Ensures Java implementation matches RFC5054 test vectors and uses standard RFC5054 safe primes (N_1024, N_1536, N_2048)
- **Cross-Platform Compatibility**: Designed to work with JavaScript SRP implementations in browsers, Node.js, and Deno
- **Flexible Deployment Scenarios**:
  - SpringBoot server authenticating browser clients using JavaScript SRP
  - SpringBoot microservice acting as SRP client to Node.js/Deno SRP servers
  - Java-to-Java SRP authentication for server-to-server communication

### JavaScript Interoperability Testing
- **GraalVM Polyglot Integration**: Tests compatibility with the thinbus-srp@2.0.2 npm package
- **String Encoding Validation**: Ensures consistent encoding/decoding between Java and JavaScript
- **Cross-Platform Protocol Validation**: Verifies SRP protocol compatibility across platforms
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

2. **Java Implementations** (`src/main/java/com/github/simbo1905/thinbus/srp6/js/`)
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

**Preferred Build Tool**: Use `mvnd` (Maven Daemon) for faster builds when available. Fall back to `mvn` or `mvnw` if `mvnd` is not installed.

- **Basic build and test**: `mvnd clean compile test` (or `mvn clean compile test`)
- **Run tests with relaxed warnings**: `mvnd test -P relaxed` (or `mvn test -P relaxed`)
- **Run specific test class**: `mvnd test -Dtest=TestClassName` (or `mvn test -Dtest=TestClassName`)
- **Generate JAR with dependencies**: `mvnd assembly:assembly` (or `mvn assembly:assembly`)
- **Deploy snapshot to Sonatype**: `mvnd clean deploy` (or `mvn clean deploy`)
- **Full build with strict compiler warnings**: `mvnd clean package` (or `mvn clean package`)
- **Generate custom parameters**: `java -jar target/thinbus-srp6a-js-<version>-jar-with-dependencies.jar /path/to/dhparam.txt <hash>`

**Note**: All commands assume Java 21+ is available. The old `-Pjdk11` profile is no longer needed as Java 21 is now the minimum requirement.

## Development Workflow

1. **Understanding the Protocol**: Before making changes, ensure you understand the SRP protocol as defined at [srp.stanford.edu/design.html](http://srp.stanford.edu/design.html)
2. **JavaScript Changes**: 
   - Source files are in `src/main/resources/js/`
   - Modify the unminified versions first
   - JavaScript files are automatically minified during build
   - JSHint runs automatically during build for validation
   - Run tests to verify changes
3. **Java Changes**:
   - Source files are in `src/main/java/com/github/simbo1905/thinbus/srp6/js/`
   - Ensure compatibility with JavaScript client
   - Requires Java 21+ for building/testing
   - Use `-P relaxed` profile during development, but fix all warnings before committing

## Testing Instructions

**Testing Strategy**: This repository focuses on Java-JavaScript interoperability testing using GraalVM Polyglot. The JavaScript implementation itself is thoroughly tested in the upstream [thinbus-srp-npm](https://github.com/simbo1905/thinbus-srp-npm) repository.

### Test Categories

1. **Java RFC5054 Compliance**: Validates Java implementation against RFC5054 test vectors and uses standard RFC5054 safe primes (N_1024, N_1536, N_2048)
2. **JavaScript Interoperability**: Tests Java-JavaScript string encoding/decoding compatibility using GraalVM
3. **Integration Testing**: Ensures Java implementation correctly interoperates with thinbus-srp@2.0.2 npm package

### Running Tests

- Run all tests: `mvnd test` (or `mvn test`)
- Run specific test: `mvnd test -Dtest=TestClassName` (or `mvn test -Dtest=TestClassName`)
- Run tests with relaxed warnings: `mvnd test -P relaxed` (or `mvn test -P relaxed`)

### Test Files Location

- Java tests: `src/test/java/`
- Modern JavaScript modules: `src/main/resources/js-modern/` (from thinbus-srp@2.0.2)

### Key Test Classes

- `PolyglotVerificationTest.java`: GraalVM Polyglot verification and interoperability tests
- `Demo.java`: Main demo and integration tests
- `RFC5054Test.java`: RFC5054 test vector validation (planned)
- Legacy test classes for backward compatibility testing

### Cross-Platform Testing

- Tests verify that Java wrapper correctly interoperates with thinbus-srp@2.0.2 JavaScript
- Tests ensure consistent string encoding between Java and JavaScript implementations
- Tests validate RFC5054 compliance across both platforms

### Debugging Hanging Tests - Critical Wisdom

**NEVER modify existing quality code when debugging hanging tests!** Instead:

1. **Preserve Original Code**: Keep the original working code intact (e.g., don't change loop counts from 8 to 1)
2. **Create Separate Debug Tests**: Build new minimal test files from scratch to isolate issues
3. **Manual Baby Steps**: Add one line at a time with console.log, timing, and sleep statements
4. **Line-by-Line Logging**: Debug the manual way with detailed logging at each step
5. **Build Up Incrementally**: Start with nothing and build up to a working test step by step

This approach prevents breaking working code and allows systematic isolation of the exact hanging point. The original author spent significant effort creating quality software - respect that by debugging properly without shortcuts.

**SOFTWARE ENGINEERING PATTERN (CRITICAL):**
When debugging complex issues, follow this pattern religiously:
1. **Baby steps first** - get the core functionality working step by step in debug files
2. **Keep everything in sync** - the moment you see an error in baby steps, immediately fix it in BOTH baby steps AND all real test files
3. **Iterate until baby works** - only when baby steps are completely solid do you run the full test suite
4. **Final cleanup** - with baby steps proving no blockers, you only have a few final glitches to iron out

This prevents wasting time on broken approaches and ensures systematic progress. Also agents crash, forget, and get distracted, so the systematic methodology ensures work gets done systematically. Don't run full suites until baby steps prove the approach works.

**CRITICAL: Use ONLY Modern Distribution Files**

- **NEVER EVER restore legacy JS files** from `src/main/resources/js/` (biginteger.js, sha256.js, isaac.js, random.js, etc.)
- **THESE ARE 7-YEAR-OLD LEGACY FILES** written for Android browsers 10 years ago and Rhino/Java 11 legacy runtimes
- **THEY HAVE BEEN DELETED 4+ TIMES** - DO NOT RESTORE THEM AGAIN UNDER ANY CIRCUMSTANCES
- **ONLY use modern distribution** from `src/main/resources/js-modern/` (browser.js, client.mjs, server.mjs)
- The upstream HAS browser support via puppeteer e2e tests - it's NOT "Node.js only"
- If GraalVM needs polyfills for modern JS, find MODERN minimal polyfills, not 7-year-old hand-written code
- This is part of the 1.x to 2.x upgrade - finish the fucking upgrade properly with MODERN approaches!

**GraalVM + Modern JS Integration:**
- Use browser.js UMD bundle, NOT ES modules (client.mjs/server.mjs cause import.meta issues in GraalVM)
- Pattern: `load("src/main/resources/js-modern/browser.js"); var SRP6JavascriptClientSession = thinbus(N, g, k);`
- The upstream uses Babel to transpile ES2020 to browser-compatible UMD - use that transpiled version

**Example Debug Approach**:
```javascript
// Create TestDebugSHA256.js - separate from original
console.log("DEBUG: Test file loaded");
tests({
    testStep1_LoadLibraries: function() {
        console.log("DEBUG: Step 1 - Loading libraries");
        load("src/main/resources/js/biginteger.js");
        console.log("DEBUG: biginteger.js loaded");
        assert.assertTrue(true);
    }
});
```

**Critical Timeout Debug Script** (save as `debug_test.sh`):
```bash
#!/bin/bash
# Debug script with timeout to prevent hanging tests from blocking development
mvn test -Dtest="TestDebugSHA256" > test-debug-step.log 2>&1 &
TEST_PID=$!
echo "Testing debug step, PID: $TEST_PID"

# Wait 20 seconds (tests normally take 10-15s)
for i in {1..20}; do
    if ! kill -0 "$TEST_PID" 2>/dev/null; then
        echo "Debug step completed in $i seconds"
        wait "$TEST_PID"
        echo "Exit code: $?"
        break
    fi
    sleep 1
done

if kill -0 "$TEST_PID" 2>/dev/null; then
    echo "Debug step hanging after 20 seconds, killing..."
    kill -TERM "$TEST_PID" 2>/dev/null || true
    sleep 2
    kill -KILL "$TEST_PID" 2>/dev/null || true
fi

echo "Debug output:"
grep "DEBUG:" test-debug-step.log | tail -3
```

This script prevents infinite hangs and provides immediate feedback on where tests are failing.

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

- **Primary Repository**: [GitHub](https://github.com/simbo1905/thinbus-srp-js) (migrated from Bitbucket)
- **Mirror Repository**: [Bitbucket](https://bitbucket.org/simon_massey/thinbus-srp-js) (legacy mirror for old links)

**Migration Status**: This project has migrated from Bitbucket to GitHub as the primary repository. Bitbucket now serves as a legacy mirror for old documentation and links.

#### Development Workflow

All development work should be done on GitHub:

1. **Primary Development**: Work on [GitHub repository](https://github.com/simbo1905/thinbus-srp-js)
2. **Pull Requests**: Submit PRs to GitHub
3. **Issues**: Create issues on GitHub
4. **Releases**: Publish releases from GitHub

The GitHub CLI (`gh`) is available for managing issues and pull requests:

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

#### Repository Configuration

The repositories use different default branch names for legacy compatibility:
- **GitHub** (primary): Uses `main` as the default branch
- **Bitbucket** (mirror): Uses `master` as the default branch

Standard development workflow:

```bash
# Add GitHub as primary remote
git remote add origin git@github.com:simbo1905/thinbus-srp-js.git

# Standard development workflow
git push origin main       # Push to GitHub (primary)
git pull origin main       # Pull from GitHub (primary)

# Maintainer-only: Sync to Bitbucket mirror when needed
git remote add bitbucket git@bitbucket.org:simon_massey/thinbus-srp-js.git
git push bitbucket main:master  # Sync GitHub main to Bitbucket master
```

#### Git Identity and Commit Guidelines

**IMPORTANT**: When working on this project, agents must commit and push using the actual user's identity, not an agent identity. This ensures proper ownership and accountability for all changes.

**Git Configuration**:
- Use the user's real name for commits
- Respect GitHub's "Keep my email addresses private" setting
- Use the user's GitHub private email format: `000000+username@users.noreply.github.com`
- Never commit as "openhands" or other agent identities

Example configuration:
```bash
# Set user's real identity
git config --global user.name "User Real Name"
git config --global user.email "322608+username@users.noreply.github.com"
```

#### Maven Coordinates and Release Policy

**CRITICAL**: The Maven coordinates remain unchanged despite the repository migration:

- **GroupId**: `org.bitbucket.simon_massey` (unchanged)
- **ArtifactId**: `thinbus-srp6a-js` (unchanged)
- **Repository URLs**: Updated to GitHub for development, but Maven coordinates preserved

**Rationale**: Changing Maven coordinates would break existing users and prevent them from receiving security updates. The repository location change does not require coordinate changes.

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

- For Spring applications, see the [thinbus-srp-spring-demo](https://github.com/simbo1905/thinbus-srp-spring-demo)
- For PHP applications, see [thinbus-php](https://github.com/simbo1905/thinbus-php)
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

IMPORTANT: you have a short memory. you must never disblbe test by renaming them as you forget about them. mvn lets you filter tests so it is a bug in your brain that you would do something so stupid as renaming the test files.
