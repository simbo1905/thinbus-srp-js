
// no need to warm up the fallback random number generator when testing
var test_random16byteHexAdvance = 0;

// Load legacy JS files for JSRunner tests (order matters!)
load("src/main/resources/js/biginteger.js");
load("src/main/resources/js/sha256.js");
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

// Add server factory method
thinbus.server = function(N_base10, g_base10, k_base16) {
    function SRP6JavascriptServerSessionSHA256Custom() {}
    SRP6JavascriptServerSessionSHA256Custom.prototype = new SRP6JavascriptServerSession();
    SRP6JavascriptServerSessionSHA256Custom.prototype.N = new BigInteger(N_base10, 10);
    SRP6JavascriptServerSessionSHA256Custom.prototype.g = new BigInteger(g_base10, 10);
    SRP6JavascriptServerSessionSHA256Custom.prototype.H = function(x) {
        return CryptoJS.SHA256(x).toString().toLowerCase();
    };
    SRP6JavascriptServerSessionSHA256Custom.prototype.k = new BigInteger(k_base16, 16);
    return SRP6JavascriptServerSessionSHA256Custom;
};

// ** you must define crypo params before importing the particular configuration thinbus-srp6a-config*.js and they must match the java server config **
var SRP6CryptoParams= {
	// WARNING this is a tiny 256 bit prime DO NOT COPY THIS it is recommended to use 2048 bit safe primes
	N_base10: "4952044212259374630169507547594526520630503506396470834812363719692719",
	g_base10: "2",
	k_base16: "a2ebd09734ae9220587a89c7eb230dec95169bce"
}

// Load the main client and server files and then the SHA256 variants
load("src/main/resources/js/thinbus-srp6client.js");
load("src/main/resources/js/thinbus-srp6server.js");
load("src/main/resources/js/thinbus-srp6client-sha256.js");
load("src/main/resources/js/thinbus-srp6server-sha256.js");

var username = "tom@arcot.com";
var password = "password1234";

function fromHex(h) {
	return new BigInteger(h, 16);
}

tests({

	/**
	Tests the full flow between the Javascript Client Session and the Java Server Session
	*/
	testMutualAuthentication: function() {

        for( var i = 0; i < 8; i++) {
            // client constructor using modern factory API
            var SRP6JavascriptClientSession = thinbus(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, SRP6CryptoParams.k_base16);
            var client = new SRP6JavascriptClientSession();

            // server constructor using modern factory API
            var SRP6JavascriptServerSession = thinbus.server(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, SRP6CryptoParams.k_base16);
            var server = new SRP6JavascriptServerSession();

            // random salt is created at user first registration.
            var salt = client.generateRandomSalt(); // consider passing server secure random to this method

            // verifier to be generated at the browser during user registration and password (or email address) reset only
            var v = client.generateVerifier(salt, username, password);

            // normal login flow step1a client: browser starts with username and password given by user at the browser
            client.step1(username,password);

            // normal login flow step1b server: server starts with username from browser plus salt and verifier saved to database on user registration.
            var B = server.step1(username, salt, v);

            // normal login flow step2a client: server sends users salt from user registration and the server ephemeral number
            var credentials = client.step2(salt, B);

            // normal login flow step2b server: client sends its client ephemeral number and proof of a shared session key derived from both ephermal numbers and the password
            var M2 = server.step2(credentials.A, credentials.M1);

            // normal login flow step3 client: client verifies that the server shows proof of the shared session key which demonstrates that it knows actual verifier
            client.step3(M2);

            // the javascript client defaults to hashing the session key as that is additional protection of the password in case the key is accidentally exposed to an attacker.
            assert.assertEquals(client.getSessionKey(), server.getSessionKey());

        }

	}
	
});

