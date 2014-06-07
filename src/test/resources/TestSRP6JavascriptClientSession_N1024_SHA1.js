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

// import script under test
load("src/main/resources/js/thinbus-srp6a-config-sha1n1024.js");

var salt = "132ce4591a29220827c6198169ea4320";
var username = "tom@arcot.com";
var password = "password1234";

// we test against the java session which uses the same string concat hashing as the javascript client
var javaServerSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSession_N1024_SHA1;

function fromHex(h) {
	return new BigInteger(h, 16);
}

tests({

	sanityCheckSha1Hash: function() {
		var input = "caebd81efad6debf9cd6128dffd66c6ad07666ce09d518c60c5ebf4fcb042b07462e390d7b52d2a4c32d2cba568a2c56b0ab919c9f4f7c79518073fabd8bd345d761c7655d227f8685ac9c065ab86929978fda40b95f5655bfdb436a75292364";
		var expected = "01e62322acba5b3ede64b4293fcd1a5bcd3a500c";
		var actual = CryptoJS.SHA1(input);
		assert.assertEquals(""+expected, ""+actual);
	}, 
	

	
	/**
	Tests the full flow between the Javascript Client Session and the Java Server Session
	*/
	testMutualAuthentiation: function() {
	
		var client = new SRP6JavascriptClientSession_N1024_SHA1();
		
		var v = client.generateVerifier(salt, username, password);
		client.step1(username,password);
		
		var server = new javaServerSession();
		var B = server.step1(username, salt, v);
		
		var credentials = client.step2(salt, B);
		
		//console.log("A:"+credentials.A);
		//console.log("M1:"+credentials.M1);
		
		var M2 = server.step2(credentials.A, credentials.M1);
		
		client.step3(M2);
	}
	
});

