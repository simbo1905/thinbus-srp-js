// import test untils
load("src/test/resources/JsUnitUtils.js");

// no need to warm up the fallback random number generator when testing
var test_random16byteHexAdvance = 0;

// import collaborators
load("src/main/resources/js/biginteger.js");
load("src/main/resources/js/sha256.js");
load("src/main/resources/js/sha1.js");
load("src/main/resources/js/isaac.js");
load("src/main/resources/js/random.js");
load("src/main/resources/js/thinbus-srp6client.js");

// ** you must define crypo params before importing the particular client session js and they must match the java server config **
var SRP6CryptoParams= {
	N_base10: "19502997308733555461855666625958719160994364695757801883048536560804281608617712589335141535572898798222757219122180598766018632900275026915053180353164617230434226106273953899391119864257302295174320915476500215995601482640160424279800690785793808960633891416021244925484141974964367107",
	g_base10: "2", 
	k_base16: "1a3d1769e1d6337af78796f1802f9b14fbc20278fb6e15e4361beb38a8e7cd3a"
}

// import script under test
load("src/main/resources/js/thinbus-srp6a-config-sha256.js");

var salt = "132ce4591a29220827c6198169ea4320";
var username = "tom@arcot.com";
var password = "password1234";

// we test the javascript client verifier generation against work-alike test java
var javaMockClient = Packages.com.bitbucket.thinbus.srp6.js.TestDouble_N1024_SHA256;

// we test against the java session which uses the same string concat hashing as the javascript client
var javaServerSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSessionSHA256;
var javaTestNumbers = Packages.com.bitbucket.thinbus.srp6.js.TestNumbers;

function fromHex(h) {
	return new BigInteger(h, 16);
}

tests({

	saltSanityCheck: function() {
		var jsClientSession = new SRP6JavascriptClientSessionSHA256();
		var randoms = [];
		for( var i = 0; i < 10; i++ ) {
			var r = jsClientSession.generateRandomSalt();
			//console.log(r);
			assert.assertTrue(r.length > 0);
			randoms.push(r);
			for( var j = i - 1; j - 1 > 0; j-- ) {
				var other = randoms[j];
				assert.assertTrue(r != other); 
			}
		}
	}, 
	
	testVerifierInputs: function() {
		var jsClientSession = new SRP6JavascriptClientSessionSHA256();
		try {
			jsClientSession.generateVerifier(null, username, password);
			fail();
		} catch(e){}
		try {
			jsClientSession.generateVerifier(salt, null, password);
			fail();
		} catch(e){}		 
		try {
			jsClientSession.generateVerifier(salt, username, null);
			fail();
		} catch(e){}	
		try {
			jsClientSession.generateVerifier("", username, password);
			fail();
		} catch(e){}
		try {
			jsClientSession.generateVerifier(salt, "", password);
			fail();
		} catch(e){}		 
		try {
			jsClientSession.generateVerifier(salt, username, "");
			fail();
		} catch(e){}			
		try {
			jsClientSession.generateVerifier(salt, username);
			fail();
		} catch(e){}
		try {
			jsClientSession.generateVerifier(salt);
			fail();
		} catch(e){}		 
		try {
			jsClientSession.generateVerifier();
			fail();
		} catch(e){}					
	},
	
	/**
	Tests the full flow between the Javascript Client Session and the Java Server Session
	*/
	testMutualAuthentiation: function() {
	
		var client = new SRP6JavascriptClientSessionSHA256();
		
		var v = client.generateVerifier(salt, username, password);
		client.step1(username,password);
		
		var server = new javaServerSession(javaTestNumbers.N, javaTestNumbers.g)
		var B = server.step1(username, salt, v);
		
		var credentials = client.step2(salt, B);
		
		//console.log("A:"+credentials.A);
		//console.log("M1:"+credentials.M1);
		
		var M2 = server.step2(credentials.A, credentials.M1);
		
		client.step3(M2);
	}
	
});

