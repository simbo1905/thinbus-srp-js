
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) client implementation for Javascript / EMCAScript in a browser talking to a Java server implementation to perform a zero knowledge proof of password. The only deployment dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There is a demonstration application [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo) written using JAX RS RESTful webservices. The demo is routinely tested with Firefox, Chrome, Safari and with an iPad. 

Code current code uses Nimbus 1.5.1-SNAPSHOT from my fork which has not yet made it to maven central so you should  `git clone` then `mvn install` that locally to use this library today. 

## Using

The jar srp6a-js-XXXX.jar contains:

  - **js/thinbus-srp6a-min.js** The all in one Javascript libraries with all dependencies minified. 
  - **js/thinbus-srp6a-config-XXX.js** Example configurations. 
  - **com/bitbucket/thinbus/srp6/js/SRP6JavascriptServerSessionXXX.class** The java classes which can interface with the javascript class. 
  
Both the Javascript and the Java **must** use matching hash algorithms (H) as well as shared prime values N, g and K. 

Configure the correct Java class using its constructor parameters {N,g}. Configure the JavaScript by creating a SRP6CryptoParams before importing one and only one of the thinbus-srp6a-config-*.js files. See how to generate your own prime number below. 

 ```
var SRP6CryptoParams= {
	N_base10: "19502997308733...
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337....
}
``` 

If you upgrade versions of the jar you must **always** extract the js from the jar and place the javascript in your webproject: 

```sh
# extract minified js file from jar
jar vxf srp6a-js-XXXX.jar js/thinbus-srp6a-min.js
```

There will be no support for running old js files against newer java files. 

## Configuration With A Custom Large Prime (N)

You should use openssl to create your own crypo parmeters. Using published RFC5054 values or other test vectors is likely a bad idea. Create our own prime with: 

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

Configure the Java session class which uses the same hash algorithm with the correct {N, g} constructor parameters. Configure the Javascript session with {N, g, k} by defining an SRP6CryptoParams holding the string values in radixes 10, 10, 16 respectively before importing a configuration which uses those values e.g. ```thinbus-srp6a-config-sha25.js``` as described above. If in doubt look at the test javascript files which configure both java and javascript and test them against each other. 

Note that SHA-256 is the strongest hash algorithm Java 1.7+ supports. You may be able to run a better one if you can configure a custom JCA. Using 1024 bit primes my four year old mac can do the crypo at between 0.05s and 0.10s depending on which of Firefox, Chrome or Safari is used. YMMV but that would suggest that you can probably use a larger bit length prime.  

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - **js/biginteger.js** BigInteger math package. 
  - **js/isaac.js** A random number generator only used if the browser does not have window.crypto secure random number generator. 
  - **js/random.js** A random number class which tries to use window.crypto or window.msCrypto random numbers else fall-backs to Isaac generator. 
  - **js/sha256.js** The Crypto.JS SHA256 hash algorithm. 
  - **js/sha1.js** The Crypto.JS SHA1 hash algorithm.   
  - **js/thinbus-srp6client.js** The SRP client session

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
