
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

	com.nimbusds.srp6.js.SRP6JavascriptServerSessionSHA1
	
Running that class as a main outputs the constants. 

Note that 'k' is the output of the servers hashing approach as hex string.  
*/

function SRP6JavascriptClientSessionSHA1(){ }

SRP6JavascriptClientSessionSHA1.prototype = new SRP6JavascriptClientSession();

SRP6JavascriptClientSessionSHA1.prototype.N = function() {
	return new BigInteger("19502997308733555461855666625958719160994364695757801883048536560804281608617712589335141535572898798222757219122180598766018632900275026915053180353164617230434226106273953899391119864257302295174320915476500215995601482640160424279800690785793808960633891416021244925484141974964367107", 10);
}

SRP6JavascriptClientSessionSHA1.prototype.g = function() {
	return new BigInteger("2", 10);
}

SRP6JavascriptClientSessionSHA1.prototype.H = function (x) {
		return CryptoJS.SHA1(x).toString().toLowerCase();
}

SRP6JavascriptClientSessionSHA1.prototype.k = new BigInteger("8d7c38a15a345fc1285b7b5a9e704e0587329ed8", 16);

