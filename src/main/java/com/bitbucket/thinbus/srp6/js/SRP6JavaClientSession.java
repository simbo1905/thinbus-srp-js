package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.nimbusds.srp6.SRP6ClientCredentials;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6ClientSession.State;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6Routines;

/**
 * If you want to have both Java clients and JavaScript clients authenticate to
 * the same Java server then this class is a workalike to the JavaScript client
 * session. This class is a thin wrapper to a Nimbus SRP6ClientSession which is
 * configured to work with the Thinbus server session.
 */
abstract public class SRP6JavaClientSession {

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
	protected final SRP6ClientSession session;

	/**
	 * Records the identity 'I' and password 'P' of the authenticating user. The
	 * session is incremented to {@link State#STEP_1}.
	 * 
	 * <p>
	 * Argument origin:
	 * 
	 * <ul>
	 * <li>From user: user identity 'I' and password 'P'.
	 * </ul>
	 * 
	 * @param userID
	 *            The identity 'I' of the authenticating user, UTF-8 encoded.
	 *            Must not be {@code null} or empty.
	 * @param password
	 *            The user password 'P', UTF-8 encoded. Must not be {@code null}
	 *            .
	 * 
	 * @throws IllegalStateException
	 *             If the method is invoked in a state other than
	 *             {@link State#INIT}.
	 */
	public void step1(String userID, String password) {
		session.step1(userID, password);
	}

	/**
	 * Receives the password salt 's' and public value 'B' from the server. The
	 * SRP-6a crypto parameters are also set. The session is incremented to
	 * {@link State#STEP_2}.
	 *
	 * <p>
	 * Argument origin:
	 * 
	 * <ul>
	 * <li>From server: password salt 's', public value 'B'.
	 * <li>From server or pre-agreed: crypto parameters prime 'N', generator 'g'
	 * and hash function 'H'.
	 * </ul>
	 *
	 * @param config
	 *            The SRP-6a crypto parameters. Must not be {@code null}.
	 * @param s
	 *            The password salt 's'. Must not be {@code null}.
	 * @param B
	 *            The public server value 'B'. Must not be {@code null}.
	 *
	 * @return The client credentials consisting of the client public key 'A'
	 *         and the client evidence message 'M1'.
	 *
	 * @throws IllegalStateException
	 *             If the method is invoked in a state other than
	 *             {@link State#STEP_1}.
	 * @throws SRP6Exception
	 *             If the session has timed out or the public server value 'B'
	 *             is invalid.
	 */
	public SRP6ClientCredentials step2(String s, String B) throws SRP6Exception {
		return session.step2(config, fromHex(s), fromHex(B));
	}

	/**
	 * Receives the server evidence message 'M1'. The session is incremented to
	 * {@link State#STEP_3}.
	 * 
	 * <p>
	 * Argument origin:
	 * 
	 * <ul>
	 * <li>From server: evidence message 'M2'.
	 * </ul>
	 * 
	 * @param M2
	 *            The server evidence message 'M2'. Must not be {@code null}.
	 * 
	 * @throws IllegalStateException
	 *             If the method is invoked in a state other than
	 *             {@link State#STEP_2}.
	 * @throws SRP6Exception
	 *             If the session has timed out or the server evidence message
	 *             'M2' is invalid.
	 */
	public void step3(String M2) throws SRP6Exception {
		session.step3(fromHex(M2));
	}

	/**
	 * Constructs a Java client session compatible with the server session which
	 * words with Java. underlying Nimbus SRP6ClientSession.
	 * 
	 * @param srp6CryptoParams
	 *            cryptographic constants which must match those being used by
	 *            the client.
	 */
	public SRP6JavaClientSession(SRP6CryptoParams srp6CryptoParams) {
		this.config = srp6CryptoParams;
		session = new SRP6ClientSession();
		session.setHashedKeysRoutine(new HexHashedURoutine());
		session.setClientEvidenceRoutine(new HexHashedClientEvidenceRoutine());
		session.setServerEvidenceRoutine(new HexHashedServerEvidenceRoutine());
		session.setXRoutine(new HexHashedXRoutine());
	}

	public String generateRandomSalt(final int numBytes) {
		byte[] bytes = SRP6Routines.generateRandomSalt(numBytes);
		MessageDigest digest = config.getMessageDigestInstance();
		digest.reset();
		digest.update(bytes, 0, bytes.length);
		BigInteger bi = new BigInteger(1, digest.digest());
		return toHex(bi);
	}

}
