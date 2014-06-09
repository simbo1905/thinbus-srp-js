
/**
This is NOT recommended class as it uses a weaker hash and the 
least bits which Nimbus SRP supports. 

On the server use the matching java class: 

	com.nimbusds.srp6.js.SRP6JavascriptServerSessionSHA1
	
Note that 'k' must be output of the servers hashing approach hex string. 
*/

function SRP6JavascriptClientSessionSHA1(){ }

SRP6JavascriptClientSessionSHA1.prototype = new SRP6JavascriptClientSession();

SRP6JavascriptClientSessionSHA1.prototype.N = function() {
	return new BigInteger(SRP6CryptoParams.N_base10, 10);
}

SRP6JavascriptClientSessionSHA1.prototype.g = function() {
	return new BigInteger(SRP6CryptoParams.g_base10, 10);
}

SRP6JavascriptClientSessionSHA1.prototype.H = function (x) {
		return CryptoJS.SHA1(x).toString().toLowerCase();
}

SRP6JavascriptClientSessionSHA1.prototype.k = new BigInteger(SRP6CryptoParams.k_base16, 16);

