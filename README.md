
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) client implementation for Javascript / EMCAScript in a browser talking to a Java server implementation to perform a zero knowledge proof of password. The only deployment dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There is a demonstration application [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo) written using JAX RS RESTful webservices. The demo is routinely tested with Firefox, Chrome, Safari and with an iPad. 

Code current code uses Nimbus 1.5.0-SNAPSHOT from my fork which has not yet made it to maven central so you should  `git clone` then `mvn install` that locally to use this library today. 

## Using

The jar `srp6a-js-XXXX.jar` contains:

  - **js/thinbus-srp6a-min.js** The all in one Javascript libraries with all dependencies minified. 
  - **js/thinbus-srp6a-config-XXX.js** Example configurations using different hashing routines. 
  - **com/bitbucket/thinbus/srp6/js/SRP6JavascriptServerSessionXXX.class** The Java classes which can interface with the Javascript. 
  
Both the Javascript and the Java **must** use matching hash algorithms `H as well as shared prime values `N`, `g`. As an extra detail the JavaScript must be configure with `k`. In SRP `k` is computed from `N` and `g`. The catch is that Nimbus uses the `java.net.BigInteger` byte array constructor to generate `k` which is not available in JavaScript. Instead use either the `OpenSSLCryptoConfig` commandline which outputs each of `N`, `g` and `k` else use the `toString()` of the Java session which will print out the values which the JavaScript must be configured with. Configuration of the Java is via the constructor. Configure the JavaScript by creating a SRP6CryptoParams before importing one and only one of the `thinbus-srp6a-config-*.js` files. 

```Javascript
var SRP6CryptoParams= {
	N_base10: "19502997308733...
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337....
}
``` 

See how to generate your own unique configuration using `openssl` below. 

There will be no support for running old js files against newer Java release. If you upgrade versions of the jar you must **always** extract the js from the jar and place the javascript **use** **it* in your web pages: 

```sh
# extract minified js file from jar
jar vxf srp6a-js-XXXX.jar js/thinbus-srp6a-min.js
```

## Configuration With A Custom Large Prime (N)

You **should** use openssl to create your own large safe prime. Using the values published under RFC5054 or other test vectors suggests seems unwise. To help with this there is a class which parses the output of an openssl command and prints out the values to use: 

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

See above as to where to set these values. If you are in any doubt read `TestSRP6JavascriptClientSessionSHA256.js` which configures java and javscript and tests them agains each other.  

Note that SHA-256 is the strongest hash algorithm Java 1.7 or 1.8 support. You may be able to use a better one if you can configure a custom JCA. Using 1024 bit primes on my four year old mac the browser takes between 0.05s and 0.10s to run the main srp work. The timings depend on which of Firefox, Chrome or Safari I am using. YMMV but this would suggest that you can probably use a prime bigger than 1024 bits.  

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - **js/biginteger.js** BigInteger math package. 
  - **js/isaac.js** A random number generator only used if the browser does not have window.crypto secure random number generator. 
  - **js/random.js** A random number class which tries to use window.crypto or window.msCrypto random numbers else fall-backs to Isaac generator. 
  - **js/sha256.js** The Crypto.JS SHA256 hash algorithm. 
  - **js/sha1.js** The Crypto.JS SHA1 hash algorithm.   
  - **js/thinbus-srp6client.js** The SRP client session

## Secure Random Numbers

The file `js/random.js` attempts to use the the WebCryptoAPI secure random number generator provided by the browsers such as IE11 and current Chrome, Firefox and Safari. If it does not find this API it then falls back to using the `js/isaac.js` random number generator. You may wish to disallow either registration, or login, or both from browsers which don't have the secure random number API. This can be checked by calling `random16byteHex.isWebCryptoAPI()`. If you do allow isaac be used with old browsers then it is recommended that you add the following 'onkeyup' to all input fields: 

```Javascript
random16byteHex.advance(Math.floor(event.keyCode/4));  
```

The `advance` method takes as input the number of milliseconds to spend spinning the random number generator. It will spin for 0.1s on page load. 

## Build Prerequisites

  - Java Platform (JDK 7+) http://www.oracle.com/technetwork/java/javase/downloads/index.html
  - Maven2 http://maven.apache.org/

## Building

```sh
git clone https://bitbucket.org/simon_massey/thinbus-srp-js
cd thinbus-srp-js
mvn package
```

Note that if you build on jdk17 the junit-js tests which test the javascript cryptography takes a very long time. It is highly recommended that you build with JDK1.8 or higher as the javascript testing is 10x faster than JDK1.7 due to the new Nashorn EMCAScript engine. 

## License

GNU GENERAL PUBLIC LICENSE Version 2, June 1991

End.
