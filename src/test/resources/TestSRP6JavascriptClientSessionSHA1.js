
// no need to warm up the fallback random number generator when testing
var test_random16byteHexAdvance = 0;

// Load legacy JS files for JSRunner tests (order matters!)
load("src/main/resources/js/biginteger.js");
load("src/main/resources/js/sha1.js");
load("src/main/resources/js/isaac.js");
load("src/main/resources/js/random.js");

// Add getBytes polyfill for JavaScript strings
String.prototype.getBytes = function() {
    var bytes = [];
    for (var i = 0; i < this.length; i++) {
        bytes.push(this.charCodeAt(i));
    }
    return bytes;
};

// Define thinbus factory function for compatibility with test
function thinbus(N_base10, g_base10, k_base16) {
    function SRP6JavascriptClientSessionSHA1Custom() {}
    SRP6JavascriptClientSessionSHA1Custom.prototype = new SRP6JavascriptClientSession();
    SRP6JavascriptClientSessionSHA1Custom.prototype.N = function() {
        return new BigInteger(N_base10, 10);
    };
    SRP6JavascriptClientSessionSHA1Custom.prototype.g = function() {
        return new BigInteger(g_base10, 10);
    };
    SRP6JavascriptClientSessionSHA1Custom.prototype.H = function(x) {
        return CryptoJS.SHA1(x).toString().toLowerCase();
    };
    SRP6JavascriptClientSessionSHA1Custom.prototype.k = new BigInteger(k_base16, 16);
    return SRP6JavascriptClientSessionSHA1Custom;
}

// ** you must define crypo params before importing the particular configuration thinbus-srp6a-config*.js and they must match the java server config **
var SRP6CryptoParams= {
	// WARNING this is a tiny 256 bit prime DO NOT COPY THIS it is recommended to use 2048 bit safe primes
	N_base10: "4952044212259374630169507547594526520630503506396470834812363719692719",
	g_base10: "2", 
	k_base16: "a2ebd09734ae9220587a89c7eb230dec95169bce"
}

// Load the main client and then the SHA1 variant
load("src/main/resources/js/thinbus-srp6client.js");
load("src/main/resources/js/thinbus-srp6client-sha1.js");

var username = "tom@arcot.com";
var password = "password1234";

// we test against the java session which uses the same string concat hashing as the javascript client
var javaServerSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSessionSHA1;

function fromHex(h) {
	return new BigInteger(h, 16);
}

tests({
	
	/**
	Tests the full flow between the Javascript Client Session and the Java Server Session.
	See the comments in the SHA256 version of this class for a fuller description.  
	*/
	testMutualAuthentication: function() {
		
//		println(username);
//		println(password);
//		println(SRP6CryptoParams.g_base10);
//		println(SRP6CryptoParams.N_base10);
//		println(SRP6CryptoParams.k_base16);
	
		// run this 64 times to catch any problems with dropping leading zeros in BigDecimal conversions to and from hex
		for( var i = 0; i < 64; i++) {
			var SRP6JavascriptClientSession = thinbus(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, SRP6CryptoParams.k_base16);
			var client = new SRP6JavascriptClientSession();
			
			var salt = client.generateRandomSalt(); // consider passing server secure random to this method
			
			//println(""+salt);
			
			var v = client.generateVerifier(salt, username, password);
			
			//println(v);
			
			client.step1(username,password);
			
			var server = new javaServerSession(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10);
			var B = server.step1(username, salt, v);
			
//			println(server.b());
//			println(B);
			
			var credentials = client.step2(salt, B);
			
//			println(credentials.A);
//			println(credentials.M1);
			
			var M2 = server.step2(credentials.A, credentials.M1);
			
//			println(M2);
			
			client.step3(M2);
			
			var cS = client.getSessionKey(false);
			
			var sS = server.getSessionKey(false);
			
			assert.assertTrue(cS == sS); 
			
			var cK = client.getSessionKey();
			
			var sK = server.getSessionKey(true);
			
			assert.assertTrue(cK == sK); 
		}
	}
});

