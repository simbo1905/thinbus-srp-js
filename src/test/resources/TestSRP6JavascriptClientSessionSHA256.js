
// no need to warm up the fallback random number generator when testing
var test_random16byteHexAdvance = 0;

// import collaborators
load("src/main/resources/js/biginteger.js");
load("src/main/resources/js/sha256.js");
load("src/main/resources/js/isaac.js");
load("src/main/resources/js/random.js");
load("src/main/resources/js/thinbus-srp6client.js");

// ** you must define crypo params before importing the particular configuration thinbus-srp6a-config*.js and they must match the java server config **
load("src/main/resources/js/rfc5054-safe-prime-config.js");
load("src/main/resources/js/thinbus-srp6client-sha256.js");

var username = "tom@arcot.com";
var password = "password1234";

// we test against the java session which uses the same string concat hashing as the javascript client
var javaServerSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSessionSHA256;

function fromHex(h) {
	return new BigInteger(h, 16);
}

tests({

	/**
	Tests the full flow between the Javascript Client Session and the Java Server Session
	*/
	testMutualAuthentiation: function() {

	    for( var i = 0; i < 8; i++) {
	
            // client constructor requires a variable SRP6CryptoParams is defined which sets N, g, k
            var client = new SRP6JavascriptClientSessionSHA256();

            // server java class for a single login
            var server = new javaServerSession(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10);

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
            assert.assertTrue(client.getSessionKey() == client.getSessionKey(true));

		}
	}
	
});

