
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a Secure Remote Password (SRP-6a) client session implementation 
for Javascript / EMCAScript in a browser talking to Java on the server to perform a zero 
knowledge proof of password. The only deployment dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

Note that if you build on jdk17 the junit-js tests which test the javascript cryptography takes a 
very long time. Building with jdk18 is ten times faster. 

N.B. Code current code uses Nimbus 1.5.0 which has not yet made it to maven central so you should  `git clone` then `mvn install` that locally to use this library today. 

## Using

See the [junit-js](http://benjiweber.co.uk/blog/2013/01/27/javascript-tests-with-junit/) test method `testMutualAuthentiation` in the test file `TestSRP6JavascriptClientSession_N1024_SHA256.js` which shows mutual authentication between Java and Javascript.  

There is a SpringMVC demonstration application [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo).

The jar srp6a-js-XXXX.jar contains:

  - js/thinbus-srp6a-min.js The all in one Javascript client with its dependencies minified. Use this as a preference where possible. 
  - com/bitbucket/thinbus/srp6/js/SRP6JavascriptServerSession_N1024_SHA256.class The java class which can interface with the javascript class. 

It is recommended that if you upgrade versions of the jar you *always* extract the js from the jar and place the javascript in your webproject: 

```sh
jar vxf srp6a-js-XXXX.jar js/thinbus-srp6a-min.js
```

There will be no support for running old js files against newer java files. 

Other source files in the jar which show the original copyright of the libraries and the un-minified client session: 

  - js/biginteger.js BigInteger math package. 
  - js/isaac.js A random number generator only used if the browser does not have window.crypto secure random number generator. 
  - js/random.js A class which uses window.crypto but will fall-back to using the Isaac generator. 
  - js/sha256.js The Crypto.JS SHA256 hash algorithm. 
  - js/thinbus-srp6client.js The SRP client session

## Build Prerequisites

  - Java Platform (JDK 7+) http://www.oracle.com/technetwork/java/javase/downloads/index.html
  - Maven2 http://maven.apache.org/

It is highly recommended that you build with JDK1.8 or higher as the javascript testing is 10x faster than JDK1.7. 

## Building

```sh
git clone https://bitbucket.org/simon_massey/thinbus-srp-js
cd thinbus-srp-js
mvn package
```

## How To Use Custom Parameters N and g

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
N base10: 19502997308733555461855666625958719160994364695757801883048536560804281608617712589335141535572898798222757219122180598766018632900275026915053180353164617230434226106273953899391119864257302295174320915476500215995601482640160424279800690785793808960633891416021244925484141974964367107
g base10:2
k base16:1a3d1769e1d6337af78796f1802f9b14fbc20278fb6e15e4361beb38a8e7cd3a
```

Configure the Java session class of the correct hash with {N, g} and configure the Javascript session with {N, g, k}. 

## License

GNU GENERAL PUBLIC LICENSE Version 2, June 1991

### TODO

```
[] Use window.crypo hashing if detected.<br/>
```

End.
