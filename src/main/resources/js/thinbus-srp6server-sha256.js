/**
This is the recommended class as it uses the strong hash which 
comes with JDK8 by default. 

You must include config which defines your safe prime constant such as SRP6CryptoParams.N_base10 before loading this file e.g.: 

var SRP6CryptoParams= {
    N_base10: "2176617...
    g_base10: "2",
    k_base16: "5b9e8ef...
}

On a Java server use the matching java class: 

	com.nimbusds.srp6.js.SRP6JavascriptServerSessionSHA256 
	
*/
function SRP6JavascriptServerSessionSHA256(){ }

SRP6JavascriptServerSessionSHA256.prototype = new SRP6JavascriptServerSession();

SRP6JavascriptServerSessionSHA256.prototype.N = new BigInteger(SRP6CryptoParams.N_base10, 10);


SRP6JavascriptServerSessionSHA256.prototype.g = new BigInteger(SRP6CryptoParams.g_base10, 10);


SRP6JavascriptServerSessionSHA256.prototype.H = function (x) {
		return CryptoJS.SHA256(x).toString().toLowerCase();
}

SRP6JavascriptServerSessionSHA256.prototype.k = new BigInteger(SRP6CryptoParams.k_base16, 16);
