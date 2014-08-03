package com.bitbucket.thinbus.srp6.js;

import static com.bitbucket.thinbus.srp6.js.HexHashedRoutines.utf8;
import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.nimbusds.srp6.SRP6CryptoParams;
/**
 * We compare the javascript client with the java logic exposed using this test
 * double.
 */
public class JavaVerifierGenerator {
	private final SRP6CryptoParams config;

	public JavaVerifierGenerator(String N, String g){
		config = new SRP6CryptoParams(SRP6JavascriptServerSession.fromDecimal(N), SRP6JavascriptServerSession.fromDecimal(g),
				SRP6JavascriptServerSessionSHA1.SHA_1);
	}


	/**
	 * Browser does string concat version of x = H(salt || H(username || ":" ||
	 * password))" Specification is RFC 5054 Which we repeat here to be able to
	 * confirm that the javascript version is working within js-unit test.
	 */
	public String hashCredentials(String salt, String identity, String password) {
		MessageDigest digest = config.getMessageDigestInstance();
		digest.reset();

		String concat = identity + ":" + password;

		digest.update(concat.getBytes(utf8));
		byte[] output = digest.digest();
		digest.reset();

		final String hash1 = toHex(new BigInteger(1, output));
		concat = (salt + hash1).toUpperCase();

		digest.update(concat.getBytes(utf8));
		output = digest.digest();

		return toHex(new BigInteger(1, output));
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
