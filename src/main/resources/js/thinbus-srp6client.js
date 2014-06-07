/*
The MIT License (MIT) http://opensource.org/licenses/MIT

Copyright (c) 2014  Simon Massey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/**
Javascript client which speaks hex strings. 
It uses random 16 byte hex strings as random key 'a'. 
*/
function SRP6JavascriptClientSession() {

	/**
	 * The session is initialised and ready to begin authentication
	 * by proceeding to {@link #STEP_1}.
	 */
	this.INIT = 0;
		
	/**
	 * The authenticating user has input their identity 'I' 
	 * (username) and password 'P'. The session is ready to proceed
	 * to {@link #STEP_2}.
	 */
	this.STEP_1 = 1;
		
	/**
	 * The user identity 'I' is submitted to the server which has 
	 * replied with the matching salt 's' and its public value 'B' 
	 * based on the user's password verifier 'v'. The session is 
	 * ready to proceed to {@link #STEP_3}.
	 */
	this.STEP_2 = 2;
		
	/**
	 * The client public key 'A' and evidence message 'M1' are
	 * submitted and the server has replied with own evidence
	 * message 'M2'. The session is finished (authentication was 
	 * successful or failed).
	 */
	this.STEP_3 = 3;
  
	this.state = this.INIT;
	
	this.x = null;
	this.v = null;
	this.I = null;
	this.P = null;
	this.B = null;
	this.A = null;
	this.a = null;
	this.k = null;
	this.u = null;
	this.S = null;
	this.M1str = null;
	
	// private
	this.check = function(v, name) {
		if( typeof v == 'undefined' || v == null || v == "" || v == "0" ) {
			throw new Error(name+" must not be null, empty or zero");
		}
	}
	
	/** private<p>
	 * 
	 * Computes x = H(s | H(I | ":" | P))
	 * <p> Uses string concatination before hashing. 
	 * <p> Specification RFC 2945
	 *
	 * @param salt     The salt 's'. Must not be null or empty.
	 * @param identity The user identity/email 'I'. Must not be null or empty.
	 * @param password The user password 'P'. Must not be null or empty
	 * @return The resulting 'x' value as BigInteger.
	 */
	this.generateX = function(salt, identity, password) {
		this.check(salt, "salt");
		this.check(identity, "identity");
		this.check(password, "password");
		var hash1 = this.H(identity+':'+password);
		var hashStr = salt+this.hash1;
		var hash = this.H(hashStr.toUpperCase());
		this.x = this.fromHex(hash).mod(this.N());
		return this.x;
	}

	/**
	 * Computes the session key S = (B - k * g^x) ^ (a + u * x) (mod N)
	 * from client-side parameters.
	 * 
	 * <p>Specification: RFC 5054
	 *
	 * @param N The prime parameter 'N'. Must not be {@code null}.
	 * @param g The generator parameter 'g'. Must not be {@code null}.
	 * @param k The SRP-6a multiplier 'k'. Must not be {@code null}.
	 * @param x The 'x' value, see {@link #computeX}. Must not be 
	 *          {@code null}.
	 * @param u The random scrambling parameter 'u'. Must not be 
	 *          {@code null}.
	 * @param a The private client value 'a'. Must not be {@code null}.
	 * @param B The public server value 'B'. Must note be {@code null}.
	 *
	 * @return The resulting session key 'S'.
	 */
	this.computeSessionKey = function(k, x, u, a, B) {
		var exp = u.multiply(x).add(a);
		var tmp = this.g().modPow(x, this.N()).multiply(k);
		return B.subtract(tmp).modPow(exp, this.N());
	}
}

// public helper
SRP6JavascriptClientSession.prototype.toHex = function(n) {
	return n.toString(16);
}

// public helper
SRP6JavascriptClientSession.prototype.fromHex = function(s) {
	var ss = ""+s; // jdk1.7 rhino requires string concat
	return new BigInteger(ss, 16);
}

// public getter
SRP6JavascriptClientSession.prototype.getState = function() {
	return this.state;
}

SRP6JavascriptClientSession.prototype.generateRandomSalt = function() {
	return random16byteHex.random();
}

/* 
 * Generates a new verifier 'v' from the specified parameters.
 * <p>The verifier is computed as v = g^x (mod N). 
 * <p> Specification RFC 2945
 *
 * @param salt     The salt 's'. Must not be null or empty.
 * @param identity The user identity/email 'I'. Must not be null or empty.
 * @param password The user password 'P'. Must not be null or empty
 * @return The resulting verifier 'v' as a hex string
 */
SRP6JavascriptClientSession.prototype.generateVerifier = function(salt, identity, password) {
	var x = this.generateX(salt, identity, password);
	this.v = this.g().modPow(x, this.N());
	return this.toHex(this.v);
}

/**
 * Records the identity 'I' and password 'P' of the authenticating user.
 * The session is incremented to {@link State#STEP_1}.
 * <p>Argument origin:
 * <ul>
 *     <li>From user: user identity 'I' and password 'P'.
 * </ul>
 * @param userID   The identity 'I' of the authenticating user, UTF-8
 *                 encoded. Must not be {@code null} or empty.
 * @param password The user password 'P', UTF-8 encoded. Must not be
 *                 {@code null}.
 * @throws IllegalStateException If the method is invoked in a state 
 *                               other than {@link State#INIT}.
 */
SRP6JavascriptClientSession.prototype.step1 = function(identity, password) {
	this.check(identity, "identity");
	this.check(password, "password");
	this.I = identity;
	this.P = password;
	if( this.state != this.INIT ) {
	  throw new Error("IllegalStateException not in state INIT");
	}
	this.state = this.STEP_1;
}

/**
 * Computes the random scrambling parameter u = H(A | B)
 * <p> Specification RFC 2945
 *
 * @param A      The public client value 'A'. Must not be {@code null}.
 * @param B      The public server value 'B'. Must not be {@code null}.
 *
 * @return The resulting 'u' value.
 */
SRP6JavascriptClientSession.prototype.computeU = function(Astr, Bstr) {
 	this.check(Astr);
 	this.check(Bstr);
	var output = CryptoJS.SHA256(Astr+Bstr);
	//console.log("jshashAB:"+output);
	return new BigInteger(""+output,16);
}

/**
 * Receives the password salt 's' and public value 'B' from the server.
 * The SRP-6a crypto parameters are also set. The session is incremented
 * to {@link State#STEP_2}.
 * <p>Argument origin:
 * <ul>
 *     <li>From server: password salt 's', public value 'B'.
 *     <li>Pre-agreed: crypto parameters prime 'N', 
 *         generator 'g' and hash function 'H'.
 * </ul>
 * @param s      The password salt 's' as a hex string. Must not be {@code null}.
 * @param B      The public server value 'B' as a hex string. Must not be {@code null}.
 * @param k      k is H(N,g) with padding by the server. Must not be {@code null}.
 * @return The client credentials consisting of the client public key 
 *         'A' and the client evidence message 'M1'.
 * @throws IllegalStateException If the method is invoked in a state 
 *                               other than {@link State#STEP_1}.
 * @throws SRP6Exception         If the public server value 'B' is invalid.
 */
SRP6JavascriptClientSession.prototype.step2 = function(s, BB) {
	this.check(s);
	//console.log("M1 js s:" + s);
	this.check(BB);
	//console.log("M1 js BB:" + BB);

	
	if( this.state != this.STEP_1 ) {
	  throw new Error("IllegalStateException not in state STEP_1");
	}
	
	this.B = this.fromHex(BB); 
	
	if (this.B.mod(this.N()).equals(BigInteger.ZERO)) {
	  throw new Error("SRP6Exception bad server public value 'B'");
	}
	
	//console.log("M1 js k:" + k);

	var x = this.generateX(s, this.I, this.P);
	//console.log("M1 js x:" + x);
	// 1024 bit N implies 512 bit key implies 2 x 16byte random implies twice salt generation
	var aStr = this.generateRandomSalt() + this.generateRandomSalt();
	this.a = this.fromHex(aStr);
	//console.log("M1 js a:" + a);
	this.A = this.g().modPow(this.a, this.N());
	//console.log("M1 js A:" + A);
	this.u = this.computeU(this.A.toString(16),BB);
	//console.log("M1 js u:" + u);
	this.S = this.computeSessionKey(this.k, x, this.u, this.a, this.B);
	
	//console.log("jsU:" + toHex(u));
	//console.log("jsS:" + toHex(S));
	
	var AA = this.toHex(this.A);
	
	//console.log("jsABS:" + AA+BB+toHex(S));
	
	this.M1str = this.H(AA+BB+this.toHex(this.S));
	
	//console.log("M1str:" + this.M1str);
	
	//console.log("M1 js A:" + AA);
	//console.log("M1 js B:" + BB);
	//console.log("M1 js S:" + toHex(S));
	//console.log("M1 jsM1:" + M1str);
	
	this.state = this.STEP_2;
	return { A: AA, M1: this.M1str };
}

/**
 * Receives the server evidence message 'M1'. The session is incremented
 * to {@link State#STEP_3}.
 *
 * <p>Argument origin:
 * <ul>
 *     <li>From server: evidence message 'M2'.
 * </ul>
 * @param serverM2 The server evidence message 'M2' as string. Must not be {@code null}.
 * @throws IllegalStateException If the method is invoked in a state 
 *                               other than {@link State#STEP_2}.
 * @throws SRP6Exception         If the session has timed out or the 
 *                               server evidence message 'M2' is 
 *                               invalid.
 */
SRP6JavascriptClientSession.prototype.step3 = function(M2) {
	this.check(M2);
	
	// Check current state
	if (this.state != this.STEP_2)
		throw new Error("IllegalStateException State violation: Session must be in STEP_2 state");

	//console.log("M2 js A:" + toHex(A));
	//console.log("M2 jsM1:" + M1str);
	//console.log("M2 js S:" + toHex(S));
	
	var computedM2 = this.H(this.toHex(this.A)+this.M1str+this.toHex(this.S));
	
	//console.log("M2 jsServerM2:" + M2);
	//console.log("M2 jsClientM2:" + computedM2);
	
	if ( ""+computedM2 != ""+M2) {
		console.log("server  M2:"+M2+"\ncomputedM2:"+computedM2);
		throw new Error("SRP6Exception Bad server credentials");
	}

	this.state = this.STEP_3;
}
