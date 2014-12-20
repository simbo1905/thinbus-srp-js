package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.nimbusds.srp6.SRP6CryptoParams;

/**
 * Generates a SRP6 verifier. Note you should use the JavaScript client not the
 * Java client for password resets from the browser. This class is only for
 * systems which let users login from java clients in addition to JavaScript
 * clients who additionally wish to implement password reset logic in their Java
 * clients. It is probably easier to implement password rest logic only via the
 * browser and have users do that if they want to reset their password which
 * they use with any Java client. Certainly you want to avoid this code ever
 * being run on the server as that would require the password to be transmitted
 * to the server which is something which SRP is designed to avoid.
 */
public class HexHashedVerifierGenerator {
	protected final SRP6CryptoParams config;

	public HexHashedVerifierGenerator(String N, String g, String hashName) {
		config = new SRP6CryptoParams(
				SRP6JavascriptServerSession.fromDecimal(N),
				SRP6JavascriptServerSession.fromDecimal(g), hashName);
	}

	/**
	 * Browser does string concat version of x = H(salt || H(username || ":" ||
	 * password))" Specification is RFC 5054 Which we repeat here to be able to
	 * confirm that the javascript version is working within js-unit test.
	 */
	public String hashCredentials(String salt, String identity, String password) {
		MessageDigest digest = config.getMessageDigestInstance();
		return HexHashedRoutines.hashCredentials(digest, salt,
				identity, password);
	}

	private BigInteger generateX(String salt, String identity, String password) {
		String hash = hashCredentials(salt, identity, password);
		return fromHex(hash).mod(config.N);
	}

	public String generateVerifier(String salt, String identity, String password) {
		BigInteger x = generateX(salt, identity, password);
		BigInteger v = config.g.modPow(x, config.N);
		return toHex(v).toLowerCase();
	}

}
