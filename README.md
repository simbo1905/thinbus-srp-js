
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) implementation for Javascript / EMCAScript to perform a zero knowledge proof of password to a Java server. The only dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There is a demonstration application [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo) written as a JAX-RS webservice. The demo has been tested with Firefox, Chrome, and Safari (on an iPad). 

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

If you upgrade versions of the jar then you must **always** extract the js from the jar and replace the script(s) your webapp uses. Alternatively you could write a servlet which serves the js directly from the jar file. There will be no support for running old js files against newer Java release. 

Choose the hashing algorithm you wish to use and then configure matching Javascript and Java SRP session objects. You **should** use your own safe prime numbers `N` and `g` as outlined below. SHA-256 is the strongest hash algorithm Java 1.7/1.8 support out of the box so it is recommended. (I have yet to try a custom JCA to enable SHA3). The Javascript SHA-256 client session configuration is in `thinbus-srp6a-config-sha256.js`. The corresponding Java server SRP session class is `SRPJavascriptServerSessionSHA256`. The Java code is configured via constructor parameters. The JavaScript code is configured by defining an `SRP6CryptoParams` object literal before you include the `thinbus-srp6a-config-sha256.js` file: 

```Javascript
var SRP6CryptoParams= {
	N_base10: "19502997308733...
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337....
}
``` 

An extra implementation detail is that the JavaScript must be configure with `k`. In the SRP protocol `k` is computed from `N` and `g` which is why the Java code does not need it. The catch is that Nimbus uses the `java.net.BigInteger` byte array constructor when generating `k`. This byte array constructor is not available in JavaScript. The `toString()` of the Java class will print each of `N`, `g` and `k` in the correct format to configure the Javascript session. 

## Creating A Custom Large Safe Prime

It is **strongly** recommended you use openssl to create your own large safe prime. To help with this there is a class which parses the output of the openssl safe prime generation command: 

```sh
# create your parameters set <bit-length> (use a minimum of 1024 bits)
openssl dhparam -text <bit-length> | tee /tmp/my_key.txt

# build the runnable jar look at the output to see the full jar name
mvn assembly:assembly

# run the jar of version <version> in the jar name to math output of build command above 
# set <hash> to the name of the algorithm e.g. "SHA-256"
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

You then use the `N` and `g` value to configure the Java session and use the `N`, `g` and `k` values to configure the Javascript session as outlined above. Also see `TestSRP6JavascriptClientSessionSHA256.js` which configures matching Java and Javascript session and tests them against each other. You could even edit that test to use your own safe prime to confirm it works before trying it out with a web server and browser. 

Using 1024 bit primes on my four year old mac the browser takes between 0.05s and 0.10s to run the main srp work. The timings depend on which of Firefox, Chrome or Safari I am using. YMMV but this would suggest that you can probably use a prime larger than 1024 bits.  

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - `js/biginteger.js` BigInteger math package. 
  - `js/isaac.js` A random number generator only used if the browser does not have window.crypto secure random number generator. 
  - `js/random.js` A random number class which tries to use window.crypto or window.msCrypto random numbers else fall-backs to Isaac generator. 
  - `js/sha256.js` The Crypto.JS SHA256 hash algorithm. 
  - `js/sha1.js` The Crypto.JS SHA1 hash algorithm.   
  - `js/thinbus-srp6client.js` The SRP client session

## Secure Random Numbers

The file `js/random.js` attempts to use the the WebCryptoAPI secure random number generator provided by modern browsers such as IE11, Chrome, Firefox and Safari. If it does not find this API it then falls back to using the `js/isaac.js` random number generator. You may wish to disallow either registration, or login, or both from browsers which don't have the secure random number WebCryptoAPI. This can be checked by calling `random16byteHex.isWebCryptoAPI()`. If you do allow isaac be used with older browsers then it is recommended that you add the following 'onkeyup' to all input fields: 

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
