package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.nimbusds.srp6.SRP6ClientCredentials;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6Routines;

/**
 * If you want to have both Java clients and JavaScript clients authenticate to
 * the same Java server then this class is a workalike to the JavaScript client
 * session.
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

	public void step1(String userID, String password) {
		session.step1(userID, password);
	}

	public SRP6ClientCredentials step2(String s, String B) throws SRP6Exception {
		return session.step2(config, fromHex(s), fromHex(B));
	}

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
