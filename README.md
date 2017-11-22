# Thinbus Javascript Secure Remote Password (SRP)
 
This package provides a Javascript [Secure Remote Password](http://srp.stanford.edu/) [SRP-6a](http://srp.stanford.edu/doc.html#papers) implementation for web browsers to perform a zero-knowledge proof-of-password to a web server. It comes with compatible Java classes which use the [Nimbus SRP6a Java](https://bitbucket.org/connect2id/nimbus-srp) library. 

There are some implementations of SRP which are compatible with the Thinbus client code. This allows you to use Thinbus client code in the broswer but other languages on the server: 

1. [thinbus-srp-spring-demo](https://bitbucket.org/simon_massey/thinbus-srp-spring-demo/overview) A Spring MVC application which uses the Thinbus JavaScript library to create accounts and login users with Spring Security. 
2. [thinbus-php](https://bitbucket.org/simon_massey/thinbus-php/overview) Uses the Thinbus Javascript library to do SRP authentication to PHP server code. It also includes a PHP SRP client that you can use for server-to-server authentication or for generating temporary passwords. 
3. [pysrp_thinbus](https://github.com/SthPhoenix/pysrp_thinbus) is a fork of [pysrp](https://github.com/cocagne/pysrp) which is compatible with Thinbus so that you can use Python on the server. 

The spring demo app has been checked on IE8+, Edge, Chrome, FireFox, and Safari. 

## CI Build Status

[ ![Codeship Status for simon_massey/thinbus-srp-js](https://codeship.com/projects/f95bffe0-3b5d-0133-b993-428ee47fa127/status?branch=master)](https://codeship.com/projects/102093)

## Maven Dependency

```
	<!-- Thinbus SRP -->
	<dependency>
		<groupId>org.bitbucket.simon_massey</groupId>
		<artifactId>thinbus-srp6a-js</artifactId>
		<version>1.5.2</version>
	</dependency>
```

## Using

Check the [Thinbus Spring Demo](https://bitbucket.org/simon_massey/thinbus-srp-spring-demo/overview). The build tool can run it locally for you. 

For the definitions of the values discussed below please refer to the [SRP design page](http://srp.stanford.edu/design.html). The following sequence diagram shows how to register a user with an SRP salt and verifier as demonstrated by the 
[Thinbus Spring Demo](https://bitbucket.org/simon_massey/thinbus-srp-spring-demo/overview). 

![Thinbus SRP Register Diagram](http://simonmassey.bitbucket.io/thinbus/register.png "Thinbus SRP Register Diagram")

In the diagram above the user is shown a standard registration form which includes both the username (e.g email) and password fields. 
They enter their email and password and click the register button. JavaScript then generates their random `salt` 
and uses the salt, email and password to generate an SRP `verififer`. Only the email, `salt` and the `verifier` are transmitted to 
the server and the generated values are saved into the database keyed by the email. 

**Note** Always use browser developer tools to inspect what you actually post to the server and only post the values shown 
in the sequence diagram as defined in the [SRP design page](http://srp.stanford.edu/design.html). It is a protocol 
violation and security bug if the raw password is accidently transmitted to the server even if it is ignored by the server. 

The following sequence diagram shows how to login a registered user. 

![Thinbus SRP Login Diagram](http://simonmassey.bitbucket.io/thinbus/login.png "Thinbus SRP Login Diagram")

In the diagram above the user is shown a standard login form. They enter their email and password and click the login button. 
JavaScript then makes an AJAX call using their email to load their `salt` and a one-time server challenge `B`. JavaScript creates 
a one-time client challenge `A` and uses all the information to compute a password proof `M1`. It then posts to the server 
the email, `A`, and `M1` as the users credentials. The server uses all the information to check the password proof. Only the email, 
client challenge `A` and the password proof `M1` are transmitted to the server. 

There is an optional step `client.step3(M2)` where `M2` is the server's proof of a shared session key to the client. 
You can return `M2` from the server to check the browser has a matching shared secret if you wish to use that for further cryptography. 
If your web application is distributed as a native mobile application such that the client is running trusted JavaScript 
then the `M2` proof is an additional check of the authenticity of the server; it confirms to trusted JavaScript that the 
server knows the verifier matching the user password. 

**Note** As per RFC 2945 the user ID (usually their email) is concatenated to their password when generating the verifier. This means that if a user changes *either* their email address or their password you need to generate a new verifier and replace the old one in the database. 

**Note** Always use browser developer tools to inspect what you actually post to the server and only post the values shown 
in the sequence diagram as defined in the [SRP design page](http://srp.stanford.edu/design.html). It is a protocol violation 
and a security bug to accidently transmit to the server anything else even if it is ignored by the server. 

**Note** the JavaScript client object (typically `SRP6JavascriptClientSessionSHA256`) must be destroyed after each login attempt. 
The object is intended to be a temporary object and should be deleted to erase all traces of the password. You must also destroy 
the password form field the user typed their password into. The normal way to achieve destroying any traces of the password is to unload 
the login page after every login attempt. This is trivial to do by reloading the login page upon authentication failure or by loading a main 
landing page upon successful login. 

**Note** that the server has to remember the private ephemeral key `b` that matches the public ephemeral key `B` sent as a one-time server challenge to the user. 
This requires storing `b` either in the database, the server session or a server cache for the short duration of the login protocol. 
You cannot pass this value back to the server from the client without compromising security. 
The server should not use any values transmitted from the client other than those shown in the sequence diagram and 
named in the [SRP design page](http://srp.stanford.edu/design.html).

**Note** if you want to use the shared session key for follow-on cryptography you should use `client.getSessionKey()` to retrieved the
session key from the thinbus object and destroy the thinbus object as discussed above. The typical way to do this is to put the session key 
into browser local session storage. Then you can unload the login page then load a main landing page that collects the session key 
from storage.  

**Note** You don't have to use AJAX for SRP. It is used in the examples to hide the fact that with SRP you need an additional round-trip to the server to generate a challenge using the users verifier. You can avoid using AJAX by splitting the username and password fields across two pages. The first page can send the username and the next page can have a hidden fields containing the user specific salt and the server challenge `B`. This simply replaces the AJAX trip with an explicit page load. 

## Custom Configuration

SHA-256 is the strongest hash algorithm Java 1.7/1.8 supports out of the box so it is recommended. The Javascript SHA-256 version is in `thinbus-srp6a-sha256-min.js`. The corresponding Java server SRP session class is `SRPJavascriptServerSessionSHA256`. The Java code is configured via constructor parameters. The JavaScript code is configured by defining an `SRP6CryptoParams` object literal before you include the main thinbus file: 

```Javascript
var SRP6CryptoParams= {
	N_base10: "19502997308733...
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337....
}
``` 

An extra implementation detail is that the JavaScript must be configure with `k`. In the SRP protocol `k` is computed from `N` and `g` which is why the Java code does not need it. The catch is that Nimbus uses the `java.net.BigInteger` byte array constructor when generating `k`. This byte array constructor is not available in JavaScript so the constant value computed by the Java must be added to the Javascript configuration. The `toString()` of the Java class will print each of `N`, `g` and `k` in the correct formats to configure the Javascript. 

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

Else you could try the online version of that tool if it is currently up and running over on the [demo server](http://thinbus-n00p.rhcloud.com/dhparam). 

You then use the `N` and `g` value to configure the Java session and use the `N`, `g` and `k` values to configure the Javascript session as outlined above. Also see `TestSRP6JavascriptClientSessionSHA256.js` which configures matching Java and Javascript sessions and tests them against one another. You should edit that test to use your own safe prime values and confirm that the test passes before attempting the use your configuration with a web browser. 

Using the demo 2048 bit prime a modern developer workstation takes less than 90ms to do the math. 
Trying out smaller 1024 bit primes on a low spec 2010 mac the browser takes between 0.05s and 0.10s to run the main srp work. 
The timings depend on which of Firefox, Chrome or Safari is used. YMMV as Javascript runtimes and mobile hardware may vary considerably 
so you should test the user experience on all the browsers you are targeting. 

## Javascript Code

Other JavaScript source files in the jar show the original copyright of the libraries and the un-minified client session: 

  - `js/biginteger.js` BigInteger math package. 
  - `js/isaac.js` A random number generator which aims to be secure. 
  - `js/random.js` A random number class which tries to use `window.crypto` or `window.msCrypto` random numbers else fall-backs to the `isaac.js` generator. 
  - `js/sha256.js` The Crypto.JS SHA256 hash algorithm. Recommended. 
  - `js/sha1.js` The Crypto.JS SHA1 hash algorithm. Optional. 
  - `js/thinbus-srp6client.js` The SRP client session

## Recommendations 

* Use Thinbus SRP over HTTPS. Configure your webserver to mark session cookies as secure to prevent accidental use of HTTP.  Configure [HSTS](https://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security#HSTS_mechanism_overview) to force HTTPS with your service. If your customers use a company supplied computer going via a corporate web proxy then HTTPS may be [decrypted and monitored](http://security.stackexchange.com/questions/63304/how-can-my-employer-be-a-man-in-the-middle-when-i-connect-to-gmail). HTTPS may be compromised due to things like [bad certs in the wild](http://nakedsecurity.sophos.com/2013/12/09/serious-security-google-finds-fake-but-trusted-ssl-certificates-for-its-domains-made-in-france/). HTTPS may be compromised by bugs or misconfigurations such as [Heartbleed](http://en.wikipedia.org/wiki/Heartbleed). HTTPS alone cannot protected against leaking passwords into error messages in your webserver logs. SRP over HTTPS is much safer than either used alone. 
* Add a javascript password strength meter and only allow users to register a verifier for a strong password. The best cryptography in the world won't protect your users if they use "password" as their password.
* Use a custom large safe prime number `N` using the instructions above. **Tip:** Check on the browsers and hardware you are targeting that the math runs fast enough for a good user experience for your chosen bit length. 
* Make the salt column in the database `not null` and add a uniqueness constraint.  
* Use symmetric AES encryption with a key only visible at the webserver to encrypt the verifier `v` value within the database. This protects against off-site database backups being used in an offline dictionary attack against `v`. 
* You can prevent privileged accounts from logging in using legacy browsers by checking `random16byteHex.isWebCryptoAPI()` when fetching the user salt; simply abort the protocol for privileged accounts when secure random numbers are not available at the browser. If you allow the use of browsers that don't have the `WebCryptoAPI` secure random number APIs then the fallback random generator hashes `window.cookie` as part of the generator seed. Consider adding a secure random cookie to help seed the fallback generator; see PRNG.md for more info.
* Don't include any JS files [or any CSS files](http://stackoverflow.com/a/3613162/329496) from external sites onto your login page. 
* Count the number of failed password attempts and present the user with a CAPTCHA after a dozen attempts. This slows down scripted online dictionary attacks. Consider suspending the account (possibly temporarily) after a large number of contiguous failed attempts to defeat someone carefully researching a user then trying to guess their likely password. 

## License

```
   Copyright 2014-2017 Simon Massey

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

Note that if you build on jdk1.7 the junit-js tests which test the javascript cryptography take a long while to run. It is highly recommended that you build with JDK1.8 or higher as the Javascript testing is 10x faster than JDK1.7 due to the Nashorn EMCAScript engine in Java1.8. 

## Thankyou

My thanks go to ej-technologies for giving free JProfiler licenses to opensource projects [ ![Profiled using JProfiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)](http://www.ej-technologies.com/products/jprofiler/overview.html)

## Release Notes

Version 1.5.2

1. Fixed JavaScript server session method `getSessionKey()`.

Version 1.5.1

1. Had accidentally released as java1.9 code when codeship.com the CI that I am using only has 1.7. Thinking about it since I don't use any features beyond 1.7 its best to release it as that version from maximum compatibility. 

Version 1.5.0

1. Added a JavaScript server SRP class. This can be used by Node.js to authenticate a client. See `TestSRP6JavascriptServerSessionSHA256.js` for usage. This is a precursor to releasing an npm version of thinbus. 

Version 1.4.2

1. Upgrades to Nimbus 2.0.2 for RFC 5054 `b` and `a` in the Java classes.

Version 1.4.1

1. Fix to issue #12 random number generator for `a` doesn't comply with RFC 5054
1. Fix to issue #13 JS client doesn't null the password after use 

Version 1.4.0

1. Fix to issue #8 problem with IE9 and other legacy browsers. 
1. Fix to issue #9 problem with IE8 not having `Date.now()`. 
1. Fix to issue #7 hash `window.cookie` into the isaac random for additional entropy. 

Version 1.3.5

1. Added missing attribution and copyright notices of 3rd party JS into header of minified library.  

Version 1.3.4

1. Added the release version into the main JS file to make it easier to track if anyone is using obsolete code. 

Version 1.3.3

1. Refactored the minified JS files to pull out the safe prime N to make it easier to provide a custom safe prime. 

Version 1.3.2

1. Renamed OpenSSLCryptoConfig to OpenSSLCryptoConfigConverter and made it more resilant to differences in the output of the openssl tools. 

Version 1.3.1

1. Refactor of OpenSSLCryptoConfig.java to be able to run it in the demo servlet in addition to being run on the commandline. 

Version 1.3.0

1. Fix to issue #3 that Java generated verifiers (e.g. your server generates temporary passwords for users) had a 6% chance of not working with the JavaScript code. This release changes the computation of `x` on the browser to drop leading zeros to be consistent with the Java. Unfortunately this means that there is a 6% chance that any users who generated verifiers using the browser using a prior release will no longer be able to login. They will have to use your password reset logic to set a fresh verifier using the latest JavaScript code.  

Version 1.2.1

1. Support of Session Serialization - update to Nimbus 1.5.3. Thanks to Bernard Wittwer. 

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
