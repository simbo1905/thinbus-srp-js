
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) 
client implementation for Javascript / EMCAScript in a browser talking to a Java server implementation to perform a zero 
knowledge proof of password. The only deployment dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There is a demonstration application [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo) written with SpringMVC. The demo is routinely tested with Firefox, Chrome, Safari and with an iPad. 

Note that if you build on jdk17 the junit-js tests which test the javascript cryptography takes a 
very long time. Building with jdk18 is ten times faster. 

Code current code uses Nimbus 1.5.0-SNAPSHOT which has not yet made it to maven central so you should  `git clone` then `mvn install` that locally to use this library today. 

## Using

See the [junit-js](http://benjiweber.co.uk/blog/2013/01/27/javascript-tests-with-junit/) test method `testMutualAuthentiation` in the test file `TestSRP6JavascriptClientSessionSHA256.js` which shows mutual authentication between Java and Javascript.  

The jar srp6a-js-XXXX.jar contains:

  - **js/thinbus-srp6a-min.js** The all in one Javascript libraries with all dependencies minified. 
  - **js/thinbus-srp6a-config-sha256n1024.js** An example of a configuration which uses SHA-256 hashing and a 1024 bit prime number. 
  - **com/bitbucket/thinbus/srp6/js/SRP6JavascriptServerSessionSHA256.class** The java class which can interface with the javascript class. 

There is also an example showing the SHA-1 algorithm. Java 1.7+ supports MD5, SHA1 and SHA-256 by default and it is recommended that you use SHA-256 (or better if you can configure a custom JCA).   

It is also recommended that you generate your own large prime number using the instructions below and configure both the Java and JavaScript to use this value. 

If you upgrade versions of the jar you must **always** extract the js from the jar and place the javascript in your webproject: 

```sh
jar vxf srp6a-js-XXXX.jar js/thinbus-srp6a-min.js
```

There will be no support for running old js files against newer java files. 

## Build Prerequisites

  - Java Platform (JDK 7+) http://www.oracle.com/technetwork/java/javase/downloads/index.html
  - Maven2 http://maven.apache.org/

It is highly recommended that you build with JDK1.8 or higher as the javascript testing is 10x faster than JDK1.7 due to the new Nashorn EMCAScript engine. 

## Building

```sh
git clone https://bitbucket.org/simon_massey/thinbus-srp-js
cd thinbus-srp-js
mvn package
```

## How To Use A Custom Large Prime (N)

You can use openssl to create your own crypo parmeters. This is highly recommended. 

```sh
# create your parameters set <bit-length> (use a minimum of 1024 bits)
openssl dhparam -text <bit-length> | tee /tmp/my_key.txt

# build the runnable jar 
mvn assembly:assembly

# run the jar of version <version> and set <hash> to the name of the algorithm e.g. SHA-256
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

Configure the Java session class with the correct {N, g} and configure the Javascript session with {N, g, k}. 

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - **js/biginteger.js** BigInteger math package. 
  - **js/isaac.js** A random number generator only used if the browser does not have window.crypto secure random number generator. 
  - **js/random.js** A random number class which tries to use window.crypto or window.msCrypto random numbers else fall-backs to Isaac generator. 
  - **js/sha256.js** The Crypto.JS SHA256 hash algorithm. 
  - **js/sha1.js** The Crypto.JS SHA1 hash algorithm.   
  - **js/thinbus-srp6client.js** The SRP client session

## License

GNU GENERAL PUBLIC LICENSE Version 2, June 1991

End.
