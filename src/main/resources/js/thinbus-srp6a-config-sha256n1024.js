
/**
This is the recommended class as it uses the strong hash which 
comes with JDK8 by default and the largest bits which Nimbus SRP
supports. 

Here we subclass and add the H, N and g for 1024 with SHA256. 
On the server use the matching java class: 

	com.nimbusds.srp6.js.SRP6JavascriptServerSession_N1024_SHA256 
	
Running that class as a main outputs the constants. Note that 'k' 
is the output of the servers hashing approach hex string. 
*/

function SRP6JavascriptClientSession_N1024_SHA256(){ 

}

SRP6JavascriptClientSession_N1024_SHA256.prototype = new SRP6JavascriptClientSession();

SRP6JavascriptClientSession_N1024_SHA256.prototype.N = function() {
	return new BigInteger("167609434410335061345139523764350090260135525329813904557420930309800865859473551531551523800013916573891864789934747039010546328480848979516637673776605610374669426214776197828492691384519453218253702788022233205683635831626913357154941914129985489522629902540768368409482248290641036967659389658897350067939", 10);
}

SRP6JavascriptClientSession_N1024_SHA256.prototype.g = function() {
	return new BigInteger("2", 10);
}

SRP6JavascriptClientSession_N1024_SHA256.prototype.H = function (x) {
		return CryptoJS.SHA256(x).toString().toLowerCase();
}


SRP6JavascriptClientSession_N1024_SHA256.prototype.k = new BigInteger("1a1a4c140cde70ae360c1ec33a33155b1022df951732a476a862eb3ab8206a5c", 16);
