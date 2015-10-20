// import test untils
load("src/test/resources/JsUnitUtils.js");

// no need to warm up the fallback random number generator when testing
var test_random16byteHexAdvance = 0;

// import collaborators
load("src/main/resources/js/biginteger.js");
load("src/main/resources/js/sha256.js");
load("src/main/resources/js/isaac.js");
load("src/main/resources/js/random.js");
load("src/main/resources/js/thinbus-srp6client.js");

// ** you must define crypo params before importing the particular configuration thinbus-srp6a-config*.js and they must match the java server config **
load("src/test/resources/js/issue3-config.js");

// import config for test
load("src/main/resources/js/thinbus-srp6client-sha256.js");

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
		// client constructor requires a variable SRP6CryptoParams is defined which sets N, g, k 
		var client = new SRP6JavascriptClientSessionSHA256();

		var salt = client.generateRandomSalt();

		var vC = client.generateVerifier(salt, username, password);
		
		var vS = (new javaHexHashedVerifierGenerator(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, "SHA-256" )).generateVerifier(salt, username, password);
		
		//console.log("vC:"+vC);
		//console.log("vS:"+vS);
		
		assert.assertTrue(vC == vS); 
		
	}
	/*
	, 
	
	testVerifierIssueRandoms: function() {
		var client = new SRP6JavascriptClientSessionSHA256();
		for( var i = 0; i < 100; i++ ){
			var salt = client.generateRandomSalt();
			var username = salt;
			var password = salt;

			var vC = client.generateVerifier(salt, username, password);
			
			var vS = (new javaHexHashedVerifierGenerator(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, "SHA-256" )).generateVerifier(salt, username, password);
			
			console.log("vC:"+vC);
			console.log("vS:"+vS);
			
			assert.assertTrue(vC == vS); 
			
		}
	}
	*/
	
});
