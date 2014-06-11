
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) implementation for Javascript / EMCAScript to perform a zero knowledge proof of password to a Java server. The only dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There is a demonstration application [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo) written as a JAX-RS webservice. The demo has been tested with Firefox, Chrome, Safari (on an iPad). 

The code current code uses Nimbus 1.5.0-SNAPSHOT from my fork which has not yet made it to maven central. So you have to `git clone` then `mvn install` my fork of Nimbus SRP to use this library today. The javascript algorithms are tested using the java scripting runtime.  

## Using

The jar `srp6a-js-XXXX.jar` contains:

  - `js/thinbus-srp6a-min.js` The all in one Javascript libraries with all dependencies minified. 
  - `js/thinbus-srp6a-config-XXX.js` Multiple example configurations. 

Extract the js files from the jar with any zip tool or with: 

```sh
# extract minified js file from jar
jar vxf srp6a-js-XXXX.jar js/thinbus-srp6a-min.js
```

If you upgrade versions of the jar then you must **always** extract the js from the jar and replace the script(s) your web pages use. Alternatively you could write a servlet which serves the js directly from the jar file. There will be no support for running old js files against newer Java release. 

Choose the hashing algorithm to use then configure the Javascript and the Java SRP session to use the same safe prime values `N` and `g`. You **should** use your own safe prime numbers see below. SHA-256 is the strongest hash algorithm Java 1.7 or 1.8 support out of the box so it is recommended. (I have yet to try a custom JCA to use SHA3). The Javascript SHA-256 client session configuration is in `thinbus-srp6a-config-sha256.js`. The corresponding Java server SRP session class is `SRPJavascriptServerSessionSHA256`. The Java code is configured via constructor parameters. The JavaScript codde is configured by defining an `SRP6CryptoParams` object literal before you include the `thinbus-srp6a-config-XXX.js` configuration file: 

```Javascript
var SRP6CryptoParams= {
	N_base10: "19502997308733...
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337....
}
``` 

See `TestSRP6JavascriptClientSessionSHA256.js` which configures matching Java and Javascript session then tests them against each other. You could even edit that test to add your own safe prime to confirm it works before trying it out wiht a web server and browser. See below for instructions how to generate your own safe prime. 

An extra implementation detail is that the JavaScript must be configure with `k`. In the SRP protocol `k` is computed from `N` and `g` which is why the Java code does not need it. The catch is that the Nimbus uses the `java.net.BigInteger` byte array constructor to generate `k` in its algorithm. This byte array constructor is not available in JavaScript. This requires that you get the Java code to show you its idea of `k` then provide it as configuration to JavaScript. The `toString()` of the Java class will print each of `N`, `g` and `k` in the format which the Javascript configuration needs. Also the instructions below to generate your own safe prime will print out the correct configuration parameters. 

## Configuration With A Custom Large Prime `N`

It is **strongly** recommended you use openssl to create your own large safe prime. To help with this there is a class which parses the output of the openssl safe prime generation command to prints out the values in the correct string encoding: 

```sh
# create your parameters set <bit-length> (use a minimum of 1024 bits)
openssl dhparam -text <bit-length> | tee /tmp/my_key.txt

# build the runnable jar 
mvn assembly:assembly

# run the jar of version <version> and set <hash> to the name of the algorithm e.g.pass "SHA-256"
java -jar target/srp6a-js-<version>-jar-with-dependencies.jar /tmp/my_key.txt <hash>
```

This will output something like: 

```
bits:1024
hashing to create 'k' using SHA-256
computing
N base10: 19502997308733...
g base10: 2
k base16: 1a3d1769e1d6337...
```

See `TestSRP6JavascriptClientSessionSHA256.js` which configures both Java and Javascript then tests them against each other. You could even edit that test to add your parameters and then run the test to confirm your safe prime configuration works before attempting to use it with a browser. 

Using 1024 bit primes on my four year old mac the browser takes between 0.05s and 0.10s to run the main srp work. The timings depend on which of Firefox, Chrome or Safari I am using. YMMV but this would suggest that you can probably use a prime bigger than 1024 bits.  

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - `js/biginteger.js` BigInteger math package. 
  - `js/isaac.js` A random number generator only used if the browser does not have window.crypto secure random number generator. 
  - `js/random.js` A random number class which tries to use window.crypto or window.msCrypto random numbers else fall-backs to Isaac generator. 
  - `js/sha256.js` The Crypto.JS SHA256 hash algorithm. 
  - `js/sha1.js` The Crypto.JS SHA1 hash algorithm.   
  - `js/thinbus-srp6client.js` The SRP client session

## Secure Random Numbers

The file `js/random.js` attempts to use the the WebCryptoAPI secure random number generator provided by the browsers  as IE11, Chrome, Firefox and Safari. If it does not find this API it then falls back to using the `js/isaac.js` random number generator. You may wish to disallow either registration, or login, or both from browsers which don't have the secure random number WebCryptoAPI. This can be checked by calling `random16byteHex.isWebCryptoAPI()`. If you do allow isaac be used with older browsers then it is recommended that you add the following 'onkeyup' to all input fields: 

```Javascript
// inside an onkeyup event handler
random16byteHex.advance(Math.floor(event.keyCode/4));  
```

The `advance` method takes as input the number of milliseconds to spend spinning the random number generator. It will spin for 100s on page load by default if it does not detect the WebCryptoAPI. 

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

## License

GNU GENERAL PUBLIC LICENSE Version 2, June 1991

End.



Instead use either the `OpenSSLCryptoConfig` commandline which outputs each of `N`, `g` and `k` else use the `toString()` of the Java session which will print out the values which the JavaScript must be configured with. Configuration of the Java is via the constructor. Configure the JavaScript by creating a SRP6CryptoParams before importing one and only one of the `thinbus-srp6a-config-*.js` files. 