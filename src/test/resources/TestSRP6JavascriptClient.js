// import test untils
load("src/test/resources/JsUnitUtils.js");

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
	N_base10: "19502997308733555461855666625958719160994364695757801883048536560804281608617712589335141535572898798222757219122180598766018632900275026915053180353164617230434226106273953899391119864257302295174320915476500215995601482640160424279800690785793808960633891416021244925484141974964367107",
	g_base10: "2", 
	k_base16: "8d7c38a15a345fc1285b7b5a9e704e0587329ed8"
}

// import config for test
load("src/main/resources/js/thinbus-srp6a-config-sha1.js");

var username = "tom@arcot.com";
var password = "password1234";

// we test against the java session which uses the same string concat hashing as the javascript client
var javaServerSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSessionSHA1;

function fromHex(h) {
	return new BigInteger(h, 16);
}

// we test the javascript client verifier generation against work-alike test java
var javaVerifierGenerator = Packages.com.bitbucket.thinbus.srp6.js.HexHashedVerifierGenerator;

// we test the java client matches the javascript client
var javaClientSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavaClientSessionSHA1;

tests({
	
	saltNoOptionalSanityCheck: function() {
		var jsClientSession = new SRP6JavascriptClientSessionSHA1();
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
	
	saltWithOptionalSanityCheck: function() {
		var jsClientSession = new SRP6JavascriptClientSessionSHA1();
		var randoms = [];
		for( var i = 0; i < 10; i++ ) {
			var r = jsClientSession.generateRandomSalt(Math.random());
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
		var jsClientSession = new SRP6JavascriptClientSessionSHA1();
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
	Tests the Javascript verifier generator against the Java version. 
	Note: See the comment on the java class that you should really choose to only ever use the Javascript version. 
	*/
	testVerifier: function() {
	
		var jsClient = new SRP6JavascriptClientSessionSHA1();
		
		var salt = jsClient.generateRandomSalt(); 
		
		var jsV = jsClient.generateVerifier(salt, username, password);
		
		var jvClient = new javaVerifierGenerator(SRP6CryptoParams.N_base10, SRP6CryptoParams.g_base10, "SHA-1");
		
		var jvV = jvClient.generateVerifier(salt, username, password);
		
		assert.assertEquals(jvV, jsV);
		
	}
});

