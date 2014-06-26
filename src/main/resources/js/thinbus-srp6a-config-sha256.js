/**
This is the recommended class as it uses the strong hash which 
comes with JDK8 by default. 
On the server use the matching java class: 

	com.nimbusds.srp6.js.SRP6JavascriptServerSessionSHA256 
	
*/

function SRP6JavascriptClientSessionSHA256(){ }

SRP6JavascriptClientSessionSHA256.prototype = new SRP6JavascriptClientSession();

SRP6JavascriptClientSessionSHA256.prototype.N = function() {
	return new BigInteger(SRP6CryptoParams.N_base10, 10);
}

SRP6JavascriptClientSessionSHA256.prototype.g = function() {
	return new BigInteger(SRP6CryptoParams.g_base10, 10);
}

SRP6JavascriptClientSessionSHA256.prototype.H = function (x) {
		return CryptoJS.SHA256(x).toString().toLowerCase();
}

SRP6JavascriptClientSessionSHA256.prototype.k = new BigInteger(SRP6CryptoParams.k_base16, 16);
