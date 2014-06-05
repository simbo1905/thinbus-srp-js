
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a Secure Remote Password (SRP-6a) client session implementation 
for Javascript / EMCAScript in a browser talking to Java on the server to perform a zero 
knowledge proof of password.

N.B. Code currently depends on the soon to be released 1.5.0 version of Nimbus SRP6a so 
you have have to git clone then mvn install that locally if you are keen to use this 
code today. 

Note that if you build on jdk17 the junit-js tests which test the javascript cryptography takes a 
very long time. Building with jdk18 is ten times faster. 

## Using

The jar srp6a-js-XXXX.jar contains:

  - js/thinbus-srp6a-min.js The all in one client and dependencies. Use this where possible. 
  - js/biginteger.js BigInteger math package. 
  - js/isaac.js A random number generator only used if the browser does not have window.crypto secure random number generator. 
  - js/random.js A class which uses window.crypto but will fall-back to using the Isaac generator. 
  - js/sha256.js The Crypto.JS SHA256 hash algorithm. 
  - js/thinbus-srp6client.js The SRP client session
  - com/bitbucket/thinbus/srp6/js/SRP6JavascriptServerSession_N1024_SHA256.class The java class which can interface with the javascript class. 

See the test method `testMutualAuthentiation` file `TestSRP6JavascriptClientSession_N1024_SHA256.js` which shows mutual authentication between Java and Javascript.  

```Javascript
git clone https://bitbucket.org/simon_massey/thinbus-srp-js
cd thinbus-srp-js
mvn package
```

## Build Prerequisites

  - Java Platform (JDK 7+) http://www.oracle.com/technetwork/java/javase/downloads/index.html
  - Maven2 http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt

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

[] Implement the smaller keysizes as pluggable config.<br/>
[] Use window.crypo hashing if detected.<br/>

End.
