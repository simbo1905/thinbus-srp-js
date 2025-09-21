
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

// Define SRP6CryptoParams constants (RFC 5054 1024-bit) BEFORE loading client files
var SRP6CryptoParams = {
    N_base10: "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819",
    g_base10: "2",
    k_base16: "5b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300"
};

// Load the main client and then the SHA256 variant
load("src/main/resources/js/thinbus-srp6client.js");
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
	testMutualAuthentication: function() {

	    for( var i = 0; i < 8; i++) {
	
            // client constructor using modern factory API
            var SRP6JavascriptClientSession = thinbus(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, SRP6CryptoParams.k_base16);
            var client = new SRP6JavascriptClientSession();

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

