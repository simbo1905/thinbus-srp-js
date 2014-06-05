
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a Secure Remote Password (SRP-6a) client session implementation 
for Javascript / EMCAScript in a browser talking to Java on the server to perform a zero 
knowledge proof of password. The only deployment dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

Note that if you build on jdk17 the junit-js tests which test the javascript cryptography takes a 
very long time. Building with jdk18 is ten times faster. 

N.B. Code current code uses Nimbus 1.5.0 which has not yet made it to maven central so you should  ```git clone``` then ```mvn install``` that locally to use this library today. 

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
  - Maven2 http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt

It is highly recommended that you build with JDK1.8 or higher as the javascript testing is 10x faster than JDK1.7. 

## Building

```sh
git clone https://bitbucket.org/simon_massey/thinbus-srp-js
cd thinbus-srp-js
mvn package
```

License
----

GNU GENERAL PUBLIC LICENSE Version 2, June 1991

TODO
----

```
[] Implement the smaller keysizes as plugable config
[] Use window.crypo hashing if detected.<br/>
```

End.
