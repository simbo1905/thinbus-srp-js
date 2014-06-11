package com.bitbucket.thinbus.srp6.js;

import java.math.BigInteger;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6ServerSession;
import com.nimbusds.srp6.SRP6ServerSession.State;

abstract class SRP6JavascriptServerSession {

	/**
	 * Increments this SRP-6a authentication session to {@link State#STEP_1}.
	 * 
	 * @param username
	 *            The identity 'I' of the authenticating user. Must not be
	 *            {@code null} or empty.
	 * @param salt
	 *            The password salt 's'. Must not be {@code null}.
	 * @param v
	 *            The password verifier 'v'. Must not be {@code null}.
	 * 
	 * @return The server public value 'B' as hex encoded number.
	 * 
	 * @throws IllegalStateException
	 *             If the mehod is invoked in a state other than
	 *             {@link State#INIT}.
	 */
	public abstract String step1(final String username, final String salt, final String v);

	/**
	 * Increments this SRP-6a authentication session to {@link State#STEP_2}.
	 * 
	 * @param A
	 *            The client public value. Must not be {@code null}.
	 * @param M1
	 *            The client evidence message. Must not be {@code null}.
	 * 
	 * @return The server evidence message 'M2' has hex encoded number with
	 *         leading zero padding to match the 256bit hash length.
	 * 
	 * @throws SRP6Exception
	 *             If the client public value 'A' is invalid or the user
	 *             credentials are invalid.
	 * 
	 * @throws IllegalStateException
	 *             If the mehod is invoked in a state other than
	 *             {@link State#STEP_1}.
	 */
	public abstract String step2(final String A, final String M1) throws Exception;

	/**
	 * Returns the underlying session state as a String for JavaScript testing.
	 * 
	 * @return The current state.
	 */
	public abstract String getState();

	/**
	 * The crypto parameters for the SRP-6a protocol. These must be agreed
	 * between client and server before authentication and consist of a large
	 * safe prime 'N', a corresponding generator 'g' and a hash function
	 * algorithm 'H'. You can generate your own with openssl using
	 * {@link OpenSSLCryptoConfig}
	 * 
	 */
	protected final SRP6CryptoParams config;

	/**
	 * The underlying Nimbus session which will be configure for JavaScript
	 * interactions
	 */
	protected final SRP6ServerSession session;
	
	/**
	 * Constructs a JavaScript compatible server session which configures an
	 * underlying Nimbus SRP6ServerSession.
	 * 
	 * @param srp6CryptoParams
	 */
	public SRP6JavascriptServerSession(SRP6CryptoParams srp6CryptoParams) {
		this.config = srp6CryptoParams;
		session = new SRP6ServerSession(config);
		session.setHashedKeysRoutine(new HexHashedURoutine());
		session.setClientEvidenceRoutine(new HexHashedClientEvidenceRoutine());
		session.setServerEvidenceRoutine(new HexHashedServerEvidenceRoutine());
	}

	/**
	 * k is actually fixed and done with hash padding routine which uses
	 * java.net.BigInteger byte array constructor so this is a convenience
	 * method to get at the Java generated value to use in the configurage of
	 * the Javascript
	 * 
	 * @return 'k' calculated as H( N, g )
	 */
	public abstract String k();

	/**
	 * Turn a radix10 string into a java.net.BigInteger
	 * 
	 * @param base10
	 * @return
	 */
	public static BigInteger fromDecimal(String base10) {
		return new BigInteger(base10, 10);
	}
}
