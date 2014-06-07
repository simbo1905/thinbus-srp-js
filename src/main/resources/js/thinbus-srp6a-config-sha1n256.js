
/**
This is NOT recommended class as it uses a weaker hash and the 
least bits which Nimbus SRP supports. It is provided just as 
a test case to show the we can configure other algorithms and 
bit sizes. 

It is HIGHLY recommended that you use the stronger class else look at the 
strongest hash your server will support (or can be upgraded to 
support with a custom JCA digest provider) and configure a matching
javascript class using the best bit length which Nimubs can provide. 

Here we subclass and add the H, N and g for 256 with SHA1. 

On the server use the matching java class: 

	com.nimbusds.srp6.js.SRP6JavascriptServerSession_N256_SHA1
	
Running that class as a main outputs the constants:

	g: 2
	N: 125617018995153554710546479714086468244499594888726646874671447258204721048803
	k: dbe5dfe0704fee4c85ff106ecd38117d33bcfe50

Note that 'k' is the output of the servers hashing approach hex string: 
*/

function SRP6JavascriptClientSession_N256_SHA1(){ }

SRP6JavascriptClientSession_N256_SHA1.prototype = new SRP6JavascriptClientSession();

SRP6JavascriptClientSession_N256_SHA1.prototype.N = function() {
	return new BigInteger("125617018995153554710546479714086468244499594888726646874671447258204721048803", 10);
}

SRP6JavascriptClientSession_N256_SHA1.prototype.g = function() {
	return new BigInteger("2", 10);
}

SRP6JavascriptClientSession_N256_SHA1.prototype.H = function (x) {
		return CryptoJS.SHA1(x).toString().toLowerCase();
}

SRP6JavascriptClientSession_N256_SHA1.prototype.k = this.fromHex("dbe5dfe0704fee4c85ff106ecd38117d33bcfe50");

