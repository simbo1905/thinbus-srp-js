
// no need to warm up the fallback random number generator when testing
var test_random16byteHexAdvance = 0;

// simulate having a cookie
var document = { cookie: "JSESSIONID=1A530637289A03B07199A44E8D531427" };

// Load legacy JS files for JSRunner tests (order matters!)
load("src/main/resources/js/biginteger.js");
load("src/main/resources/js/sha256.js");
load("src/main/resources/js/isaac.js");
load("src/main/resources/js/random.js");

// ** you must define crypo params before importing the particular configuration thinbus-srp6a-config*.js and they must match the java server config **
load("src/test/resources/js/issue3-config.js");

// Load the main client and then the SHA256 variant
load("src/main/resources/js/thinbus-srp6client.js");
load("src/main/resources/js/thinbus-srp6client-sha256.js");

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
    function SRP6JavascriptClientSessionSHA256Custom() {}
    SRP6JavascriptClientSessionSHA256Custom.prototype = new SRP6JavascriptClientSession();
    SRP6JavascriptClientSessionSHA256Custom.prototype.N = function() {
        return new BigInteger(N_base10, 10);
    };
    SRP6JavascriptClientSessionSHA256Custom.prototype.g = function() {
        return new BigInteger(g_base10, 10);
    };
    SRP6JavascriptClientSessionSHA256Custom.prototype.H = function(x) {
        return CryptoJS.SHA256(x).toString().toLowerCase();
    };
    SRP6JavascriptClientSessionSHA256Custom.prototype.k = new BigInteger(k_base16, 16);
    return SRP6JavascriptClientSessionSHA256Custom;
}

var username = "qa_poc_2_user-0-33671@leshop.ch";
var password = "asdasd";

// we test against the java session which uses the same string concat hashing as the javascript client
var javaServerSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSessionSHA256;
var javaHexHashedVerifierGenerator = Packages.com.bitbucket.thinbus.srp6.js.HexHashedVerifierGenerator;

function fromHex(h) {
	return new BigInteger(h, 16);
}

tests({

	testVerifierIssue3: function() {
		// client constructor using modern factory API
		var SRP6JavascriptClientSession = thinbus(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, SRP6CryptoParams.k_base16);
		var client = new SRP6JavascriptClientSession();

		var salt = client.generateRandomSalt();

		var vC = client.generateVerifier(salt, username, password);
		
		var vS = (new javaHexHashedVerifierGenerator(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, "SHA-256" )).generateVerifier(salt, username, password);
		
		//console.log("vC:"+vC);
		//console.log("vS:"+vS);
		
		assert.assertTrue(vC == vS); 
		
	}

});
