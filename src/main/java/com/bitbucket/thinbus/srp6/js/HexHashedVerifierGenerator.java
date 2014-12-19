package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.nimbusds.srp6.SRP6CryptoParams;

/**
 * We compare the javascript client with the java logic exposed using this test
 * double.
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
