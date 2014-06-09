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
var javaServerSession = Packages.com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSessionSHA1;
var javaTestNumbers = Packages.com.bitbucket.thinbus.srp6.js.TestNumbers;

function fromHex(h) {
	return new BigInteger(h, 16);
}

tests({
	
	/**
	Tests the full flow between the Javascript Client Session and the Java Server Session
	*/
	testMutualAuthentiation: function() {
	
		var client = new SRP6JavascriptClientSessionSHA1();
		
		var v = client.generateVerifier(salt, username, password);
		client.step1(username,password);
		
		var server = new javaServerSession(javaTestNumbers.N, javaTestNumbers.g);
		var B = server.step1(username, salt, v);
		
		var credentials = client.step2(salt, B);
		
		//console.log("A:"+credentials.A);
		//console.log("M1:"+credentials.M1);
		
		var M2 = server.step2(credentials.A, credentials.M1);
		
		client.step3(M2);
	}
	
});

