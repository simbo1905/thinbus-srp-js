
/**
This is NOT recommended class as it uses a weaker hash and the 
least bits which Nimbus SRP supports. It is provided just as 
a test case to show the we can configure other algorithms and 
bit sizes. 

It is HIGHLY recommended that you use the stronger class else look at the 
strongest hash your server will support (or can be upgraded to 
support with a custom JCA digest provider) and configure a matching
javascript class using the best bit length which Nimubs can provide. 

Here we subclass and add the H, N and g for 1024 with SHA1. 

On the server use the matching java class: 

	com.nimbusds.srp6.js.SRP6JavascriptServerSession_N1024_SHA1
	
Running that class as a main outputs the constants. 

Note that 'k' is the output of the servers hashing approach as hex string.  
*/

function SRP6JavascriptClientSession_N1024_SHA1(){ }

SRP6JavascriptClientSession_N1024_SHA1.prototype = new SRP6JavascriptClientSession();

SRP6JavascriptClientSession_N1024_SHA1.prototype.N = function() {
	return new BigInteger("167609434410335061345139523764350090260135525329813904557420930309800865859473551531551523800013916573891864789934747039010546328480848979516637673776605610374669426214776197828492691384519453218253702788022233205683635831626913357154941914129985489522629902540768368409482248290641036967659389658897350067939", 10);
}

SRP6JavascriptClientSession_N1024_SHA1.prototype.g = function() {
	return new BigInteger("2", 10);
}

SRP6JavascriptClientSession_N1024_SHA1.prototype.H = function (x) {
		return CryptoJS.SHA1(x).toString().toLowerCase();
}

SRP6JavascriptClientSession_N1024_SHA1.prototype.k = new BigInteger("7556aa045aef2cdd07abaf0f665c3e818913186f", 16);

