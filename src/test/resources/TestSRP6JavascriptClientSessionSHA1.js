
// no need to warm up the fallback random number generator when testing
var test_random16byteHexAdvance = 0;

// import collaborators
load("src/main/resources/js/biginteger.js");
load("src/main/resources/js/sha1.js");
load("src/main/resources/js/isaac.js");
load("src/main/resources/js/random.js");
load("src/main/resources/js/thinbus-srp6client.js");

// ** you must define crypo params before importing the particular configuration thinbus-srp6a-config*.js and they must match the java server config **
var SRP6CryptoParams= {
	// WARNING this is a tiny 256 bit prime DO NOT COPY THIS it is recommended to use 2048 bit safe primes
	N_base10: "4952044212259374630169507547594526520630503506396470834812363719692719",
	g_base10: "2", 
	k_base16: "a2ebd09734ae9220587a89c7eb230dec95169bce"
}

// import config for test
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
	testMutualAuthentiation: function() {
		
//		println(username);
//		println(password);
//		println(SRP6CryptoParams.g_base10);
//		println(SRP6CryptoParams.N_base10);
//		println(SRP6CryptoParams.k_base16);
	
		// run this 100 times to catch any problems with dropping leading zeros in BigDecimal conversions to and from hex
		for( var i = 0; i < 100; i++) {
			var client = new SRP6JavascriptClientSessionSHA1();
			
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

