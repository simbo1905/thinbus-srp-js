# Thinbus Javascript Secure Remote Password (SRP) 

Copyright (c) Simon Massey, 2014-2015
 
This package provides a Javascript [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) implementation for web browsers to perform a zero-knowledge proof-of-password to a web server. It comes with compatible Java classes but there is also a demo using PHP server code. The server Java code only has a dependency on the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There are a number of demonstration applications: 

1. [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo) Is a minimal demo using JSON over AJAX to a simple JAX-RS webservice. The demo has been tested with Firefox, Chrome, and Safari (on an iPad). 
2. [thinbus-php](https://bitbucket.org/simon_massey/thinbus-php/overview) Uses the Thinbus Javascript library to do SRP authentication to PHP server code. 
3. [thinbus-srp-spring-demo](https://bitbucket.org/simon_massey/thinbus-srp-spring-demo/overview) A Spring MVC application which uses the Thinbus JavaScript library to create accounts and login users with Spring Security. 

## Maven Dependency

```
	<!-- Thinbus SRP -->
	<dependency>
		<groupId>org.bitbucket.simon_massey</groupId>
		<artifactId>thinbus-srp6a-js</artifactId>
		<version>1.2.0</version>
	</dependency>
```

## Quick Start

Check the `src/main/webapp/lib/*.js` files in the [demo application](https://bitbucket.org/simon_massey/thinbus-srp-js-demo). That demo app is using the files extracted from the built thinbus-srp6a-js-<version>.jar:

  - `js/thinbus-srp6a-min.js` All the required dependencies minified. 
  - `js/thinbus-srp6a-2048-sha256-min.js` Example minified configuration using the RFC 5054 2048 bit prime and the sha256 hashing algorithm.  

See `TestSRP6JavascriptClientSessionSHA256.js` which configures matching Java and Javascript sessions and tests them with the JDK Javascript runtime using [JUnit-JS](http://benjiweber.co.uk/blog/2013/01/27/javascript-tests-with-junit/). 

**Note** If you use the java server code if you upgrade versions of the Java jar version you must **always** extract the main js file from the jar and refresh the script(s) the browser uses. Alternatively you could write a servlet which serves the js directly from the jar file. There will be no support for running old js logic against a newer Java logic. 

## Custom Configuration

SHA-256 is the strongest hash algorithm Java 1.7/1.8 supports out of the box so it is recommended. The Javascript SHA-256 client session configuration is in `thinbus-srp6a-config-sha256.js`. The corresponding Java server SRP session class is `SRPJavascriptServerSessionSHA256`. The Java code is configured via constructor parameters. The JavaScript code is configured by defining an `SRP6CryptoParams` object literal before you include the `thinbus-srp6a-config-sha256.js` file: 

```Javascript
var SRP6CryptoParams= {
	N_base10: "19502997308733...
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337....
}
``` 

An extra implementation detail is that the JavaScript must be configure with `k`. In the SRP protocol `k` is computed from `N` and `g` which is why the Java code does not need it. The catch is that Nimbus uses the `java.net.BigInteger` byte array constructor when generating `k`. This byte array constructor is not available in JavaScript so the value computed by the Java must be added to the Javascript. The `toString()` of the Java class will print each of `N`, `g` and `k` in the correct formats to configure the Javascript. 

## Creating A Custom Large Safe Prime

You can use openssl to create your own large safe prime. To help with this there is a class which parses the output of the openssl safe prime generation command: 

```sh
# create your parameters set <bit-length> (recommended minimum of 2048)
openssl dhparam -text <bit-length> | tee /tmp/my_dhparam.txt

# build the runnable jar-with-dependencies 
mvn assembly:assembly

# use the jar name which matches the output of the assembly command. set <hash> to the name of the java hashing algorithm to use e.g. "SHA-256"
java -jar target/thinbus-srp6a-js-<version>-jar-with-dependencies.jar /tmp/my_dhparam.txt <hash>
```

This will output something like: 

```sh
bits:1024
hashing to create 'k' using SHA-256
computing
N base10: 19502997308733...
g base10: 2
k base16: 1a3d1769e1d6337...
```

You then use the `N` and `g` value to configure the Java session and use the `N`, `g` and `k` values to configure the Javascript session as outlined above. Also see `TestSRP6JavascriptClientSessionSHA256.js` which configures matching Java and Javascript session and tests them against one another. You should edit that test to use your own safe prime values and confirm that the test passes before testing with a web browser. 

Using the demo 2048 bit prime a modern developer workstation takes less than 90ms to do the math. Trying out smaller 1024 bit primes on a four year old mac the browser takes between 0.05s and 0.10s to run the main srp work. The timings depend on which of Firefox, Chrome or Safari is used. YMMV as Javascript runtimes and mobile hardware may vary considerably so you should test the user experience on all your browsers you are targeting. 

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - `js/biginteger.js` BigInteger math package. 
  - `js/isaac.js` A random number generator which aims to be secure. 
  - `js/random.js` A random number class which tries to use `window.crypto` or `window.msCrypto` random numbers else fall-backs to the `isaac.js` generator. 
  - `js/sha256.js` The Crypto.JS SHA256 hash algorithm. Recommended. 
  - `js/sha1.js` The Crypto.JS SHA1 hash algorithm. 
  - `js/thinbus-srp6client.js` The SRP client session

## Recommendations 

* Make the salt column in the database `not null` and add a uniqueness constraint.  
* Use symmetric encryption with a key only visible at the webserver to encrypt the verifier `v` value within the database. This protects against off site database backups being used in an offline dictionary attack against `v`. 
* If you allow the use of Issac as a fallback random number generator add `onkeyup` event handlers which advance the random stream as documented above. 
* Add a javascript password strength meter to the register form to encourage users to use strong passwords. The best cryptography in the world won't protect your users if they use "password" as their password. Consider only allowing them to register with strong  passwords to make an [online dictionary attack infeasible](http://xkcd.com/936/). 
* Use Thinbus SRP over HTTPS. Configure your Webserver to mark session cookies as secure so that uses won't be able to login except over HTTPS to prevent accident use of HTTP. If your customers use a company supplied computer going via a corporate web proxy then HTTPS may be [decrypted and monitored](https://www.bluecoat.com/products/proxysg). HTTPS may be compromised due to things like [bad certs in the wild](http://nakedsecurity.sophos.com/2013/12/09/serious-security-google-finds-fake-but-trusted-ssl-certificates-for-its-domains-made-in-france/). HTTPS may be compromised by bugs or misconfigurations such as [Heartbleed](http://en.wikipedia.org/wiki/Heartbleed). So HTTPS alone cannot protected against leaking passwords into error messages on your webserver or database server logs. SRP over HTTPS is better than either used alone. 
* Create a custom large safe prime number `N`. **Tip:** Check on the browsers and hardware you are targeting that the math runs fast enough for a good user experience for your chosen bit length. 

## License

```
   Copyright 2014-2015 Simon Massey

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```   

## Build Prerequisites

  - Java Platform (JDK 7+) http://www.oracle.com/technetwork/java/javase/downloads/index.html
  - Maven2 http://maven.apache.org/

## Building

```sh
git clone https://bitbucket.org/simon_massey/thinbus-srp-js
cd thinbus-srp-js
mvn package
```

Note that if you build on jdk17 the junit-js tests which test the javascript cryptography take a long while to run. It is highly recommended that you build with JDK1.8 or higher as the Javascript testing is 10x faster than JDK1.7 due to the new Nashorn EMCAScript engine in Java1.8. 

## Footnote: Random Numbers At The Browser

This section is an advanced discussion about how Thinbus generates and uses random numbers which most people can skip over. 

Typically cryptography depends on secure random numbers and historically this has been a weak point with browsers. Thinbus tries to use the moden `WebCryptoAPI` secure random number API in modern browsers have. If that is not available it falls back to a pure javascript isaac random number generator which is warmed up upon page load. The reasoning as to why I consider this acceptable is as follows. 

An SRP6a proof of password uses three numbers `s`, `a`, `b` which are specified to be random: 

1. The value `b` is the server ephemeral one time key. This is used to compute `B` which is sent from server to browser as the unique challenge per login attempt. Thinbus uses Nimbus which uses the Java secure random number generator for this value. 
1. The salt `s` is created at user registration then stored on the server. This is a public value as anyone claiming to be the user is given the salt to perform the proof of password. Thinbus provides an optional salt generation method to use at the browser. You don't have to use this method. You can choose to use a salt entirely generated at the server, one which is generated entirely at the browser, else one which is a server random hashed into a browser random. These options are described below.  
1. The value `a` is created by the browser as the client ephemeral one time key for a single login attempt. This is then used to compute `A` which is sent from the browser to the server. There are no options here and the approach taken by Thinbus is detailed below. 

The salt `s` is a public value in the protocol which is fixed per user and would be stored in the user database. The desired property is that it is unique for every user in your system. This can be ensured by adding a uniqueness constraint to a `not null` salt column within the database which is **strongly recommended**. Then it does not matter whether this public value has been generated using a good secure random number at the server or using a weaker random number generator at the browser. You simply reduce the probability of database constraint exceptions if you use a better random number. Thinbus provides a method `generateRandomSalt` to run at the browser to create `s` which can be invoked with, or without, passing a sever generated secure random number or avoided entirely by generating the salt at the server. It hashes `Date.now()` with a browser random and the optional server random to avoid a total failure to come up with values which do not repeat between user registrations. 

The property of `a` which we desire is that it does not repeat between login attempts. The user could be redirected to a malicious server which is forcing multiple login attempts with a crafted `B` to attack the password. This requires that `a` be random to not leak information. It **must not** be passed by the server else a malicious server could pass known `a`, `s` and `B` for which it has pre-computed a rainbow table which takes `M1` as the lookup value. Thinbus hashes `Date.now()` into the browser random to formulate an `a` value which will then vary for subsequent login attempts even if the browser has a faulty random number generator.  

Currently IE11, Chrome, Firefox and Safari each implement a version of the secure random number generator in the WebCryptoAPI draft standard. If `window.crypto` or `window.msCrypto` is not detected Thinbus uses an [Isaac](http://en.wikipedia.org/wiki/ISAAC_%28cipher%29) generator discarding random numbers in a busy loop for 0.1s at page load. The discarding of randoms is to warm up the isaac generator as the published cryptanalysis indicates that with too uniform a starting array the early numbers may not be random enough. You can detected the use of Isaac by checking whether `random16byteHex.isWebCryptoAPI()` returns false should you wish to abort and tell the user to use a better browser. As noted above `Date.now()` is hashed into the pseudorandom which IMHO makes Issac an acceptable option for older browsers that don't provide WebCryptoAPI secure random numbers. If you do allow the use of Isaac it is **recommended** that you spin it forward using an `onkeyup` event handler attached to the username and password input fields to further warm it up: 

```Javascript
function (event) {
  /* drops randoms in a loop for some millesconds based on the key being pressed */
  random16byteHex.advance(Math.floor(event.keyCode/4));
}
```

## Release Notes

Version 1.2.0

1. Exposed `userId` on the javascript client session
2. Added `getSessionKey` on the javascript client 
3. Changed the `getSessionKey(true)` java code to do hashing which matches the javascript and PHP versions.

Version 1.1.1

1. Added getters to be able to access the SRP paramaters outside of the anticipated login flow.  

Version 1.1.0

1. Added a Java client session which matches the Java server session. This allows for users to verify via either a Java client or a Javascript client with the same verifier. See JavaClientTests.java for usage. 
2. Added HexHashedVerifierGenerator which allows a Java client to create a verifier identical to that generated by the Javascript client. This allows users to do a password reset by either Java or Javascript. 
3. Renamed the artifact to be thinbus-srp6a-js.

Version 1.0.2

1. Javascript client now aborts on u==0. 
2. Javascript now strips off leading zeros in hashed values which caused login failures comparing with server hex values as the Java BigInteger class strips leading zeros.  

Version 1.0.1

A critical defect was found in the 1.0.0 js logic. Please upgrade to >=1.0.1 immediately. To prevent a regression a test has been added which tests the javascript password algorithm against identical logic implemented in java. The project has also now been configured to use [JsHint](http://www.jshint.com/docs/). This fails the build for the sort of bug which javascript is silent about but a java compiler would notice such as the critical bug.