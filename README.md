
# Thinbus Secure Remote Password (SRP-6a) 

Copyright (c) Simon Massey, 2014
 
This package provides a [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) implementation for Javascript / EMCAScript to perform a zero knowledge proof of password to a Java server. The only dependency is the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There is a demonstration application [thinbus-srp-js-demo](https://bitbucket.org/simon_massey/thinbus-srp-js-demo) written as a JAX-RS webservice. The demo has been tested with Firefox, Chrome, and Safari (on an iPad). 

The code current code uses Nimbus 1.5.0-SNAPSHOT from my fork which has not yet made it to maven central. So you have to `git clone` then `mvn install` my fork of Nimbus SRP to use this library today. The javascript algorithms are tested using the java scripting runtime.  

## Using

The jar `srp6a-js-XXXX.jar` contains:

  - `js/thinbus-srp6a-min.js` All the required dependencies minified. 
  - `js/js/shaXXX-min.js` Hashing algorithms. You must pick and include one. The recommended one is sha256. 
  - `js/thinbus-srp6a-config-XXX.js` Multiple example configurations. You must include one which matches the chosen hashing algorithm. The recommended one is sha256. 

Choose the hashing algorithm you wish to use and then configure matching Javascript and Java SRP session objects. SHA-256 is the strongest hash algorithm Java 1.7/1.8 support out of the box so it is recommended. The Javascript SHA-256 client session configuration is in `thinbus-srp6a-config-sha256.js`. The corresponding Java server SRP session class is `SRPJavascriptServerSessionSHA256`. The Java code is configured via constructor parameters. The JavaScript code is configured by defining an `SRP6CryptoParams` object literal before you include the `thinbus-srp6a-config-sha256.js` file: 

```Javascript
var SRP6CryptoParams= {
	N_base10: "19502997308733...
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337....
}
``` 

It is recommended that after you have a working setup that you investigate the performance of custom large safe prime numbers `N` and `g`. How to create and configure your own large prime is outlined below. Consider using a safe prime larger than 1024 bits for increased security. This requires some testing the browsers and hardware you are targeting to check that the math runs fast enough for a good user experience.  

Extract the js files from the jar with any zip tool e.g.: 

```sh
# extract minified js file from jar
jar vxf srp6a-js-XXXX.jar js/thinbus-srp6a-min.js
```

If you upgrade versions of the jar then you must **always** extract the js file from the jar and replace the script(s) your webapp uses. Alternatively you could write a servlet which serves the js directly from the jar file. There will be no support for running old js files against newer Java release. 

An extra implementation detail is that the JavaScript must be configure with `k`. In the SRP protocol `k` is computed from `N` and `g` which is why the Java code does not need it. The catch is that Nimbus uses the `java.net.BigInteger` byte array constructor when generating `k`. This byte array constructor is not available in JavaScript so the value computed by the Java must be added to the configuration fo the Javascript. The `toString()` of the Java class will print each of `N`, `g` and `k` in the correct format to configure the Javascript session. 

## Creating A Custom Large Safe Prime

It is recommended you use openssl to create your own large safe prime which is larger that 1024 bits. To help with this there is a class which parses the output of the openssl safe prime generation command: 

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

You then use the `N` and `g` value to configure the Java session and use the `N`, `g` and `k` values to configure the Javascript session as outlined above. Also see `TestSRP6JavascriptClientSessionSHA256.js` which configures matching Java and Javascript session and tests them against each other. You could even edit that test to use your own safe prime to confirm it the test passes before trying it out with a web server and browser. 

Using 1024 bit primes on my four year old mac the browser takes between 0.05s and 0.10s to run the main srp work. The timings depend on which of Firefox, Chrome or Safari I am using. YMMV as Javascript runtimes and mobile hardware may vary considerably so you should test comprehensively even if you are sticking with the provided default `N`. 

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - `js/biginteger.js` BigInteger math package. 
  - `js/isaac.js` A random number generator which aims to be secure. 
  - `js/random.js` A random number class which tries to use window.crypto or window.msCrypto random numbers else fall-backs to Isaac generator. 
  - `js/sha256.js` The Crypto.JS SHA256 hash algorithm. 
  - `js/sha1.js` The Crypto.JS SHA1 hash algorithm.   
  - `js/thinbus-srp6client.js` The SRP client session

## Secure Random Numbers

Thinbus tries to use the browsers `window.crypto` or `window.msCrypto` secure random number generator. If that is not available it falls back to an isaac random number generator. Typically if you are deploying outside of a corporate network you cannot control the browser environment; so even if the browser has the secure random API it might be faulty and return pseudo randoms or even a constant value. To counter this risk Thinbus hashes the browser generated random with other values to avoid the risk of repeated values being used for successive user logins attempts made from the same browser. This is discussed in detail below. 

An SRP proof of password uses three numbers `s`, `a`, `b` which are specified to be random: 

1. The value `b` is the server ephemeral one time key. This is used to compute `B` which is sent from server to browser as the unique challenge per login attempt. Thinbus uses the Java secure random number generator for this value. 
1. The salt `s` is created at user registration then stored on the server. Thinbus provides an optional salt generation method to use at the browser. You don't have to use this method. You can choose to use a salt entirely generated entirely at the server, a salt which is generated entirely at the browser, else a hybrid salt which is a server generated random hashed into a browser generated random. These options are described below.  
1. The value `a` is created by the browser as the client ephemeral one time key for a single login attempt. This is then used to compute `A` which is sent from the browser to the server. There are no options here and the approach taken by Thinbus is detailed below. 

The salt `s` is a public value in the protocol which is fixed per user and is stored in the database. The desired property is that it is unique for every user in your system. This can be ensured by adding a uniqueness constraint to the salt column within the database which is **strongly recommended** (it should also be a 'NOT NULL' column). Then in my view (YMMV) it matters not whether this public value has been generated by the server using a secure random number with a very low risk of collision or at the browser with a pseudo random number generator with a much higher risk of a collision. Thinbus therefore provides a method `generateRandomSalt` to run at the browser which can be invoked with or without passing a sever generated secure random number. It hashes `Date.now()` with a browser random and the optional server random. You may choose to generate the salt solely on the server and bypass this method entirely. The use of `Date.now()` and the hashing algorithm should avoid a total failure to come up with or get a random value to the browser. Yet you should still add a unique constraint to the not null salt column in the database to counter the risk of any bugs in saving the salt into the database. 

The property of `a` which we desire is that it does not repeat between login attempts. The user could be redirected to a malicious server which is forcing multiple login attempts with a crafted `B` to attack the password. This requires that `a` be random to not leak information and that `a` must be generated at the browser. It **must not** be passed by the server else a malicious server could pass known `a`, `s` and `B` for which it has pre-computed a rainbow table which takes `M1` as the lookup value. Thinbus hashes the username into `x` (and therefore `M1`) making this attack less easy should `a` not be perfectly random. To counter any future bugs that their may be with `window.crypto` implementations returning a constant or pseudorandom number Thinbus hashes `Date.now()` into the browser random to formulate an `a` value which will then vary for subsequent login attempts if the browser randon number generator is faulty.  

Note that if `window.crypto` or `window.msCrypto` is not detected Thinbus users an Isaac generator with a drop algorithm which discards the random numbers in a busy loop for 0.1s. As noted above steps are taken to guard against pseudorandom or constant values being generated at the browser making (IMHO/YMMV) Issac a reasonable option. You can disallow the use of Isaac by checking `random16byteHex.isWebCryptoAPI()`. If you do use Isaac you can spin it forward using an 'onkeyup' event handler on the username and password fields with an event handler like: 

```Javascript
function (event) {
  // drops randoms in a loop for less than 0.1s defined by use key pressed
  random16byteHex.advance(Math.floor(event.keyCode/4));
}

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

## Recommendations 

* Use Thinbus SRP over HTTPS. HTTPS may be compromised due to things like [bad certs in the wild](http://nakedsecurity.sophos.com/2013/12/09/serious-security-google-finds-fake-but-trusted-ssl-certificates-for-its-domains-made-in-france/). HTTPS may be compromised by bugs or misconfigurations such as [Heartbleed](http://en.wikipedia.org/wiki/Heartbleed). HTTPS may be perfect for your site but in the future may leak passwords into error messages in the logs which malicious people can get at. So SRP over HTTPS is better than either used alone. 
* blah blah
* Consider using Thinbus SRP with two factor authentication. Two factor authentication is at risk of social engineering attacks as described in the book (The Art Of Deception)[http://en.wikipedia.org/wiki/The_Art_of_Deception]. So SRP with two factor authentication is better than two factor authentication used alone. 

## License

GNU GENERAL PUBLIC LICENSE Version 2, June 1991

## TODO

```sh
Hash time into the `a`. 
```

End.
