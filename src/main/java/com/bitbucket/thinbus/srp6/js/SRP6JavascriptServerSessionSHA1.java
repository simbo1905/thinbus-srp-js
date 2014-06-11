package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Routines;

/**
 * Wrapper of a server session matching the Javascript client session
 * SRP6JavascriptClientSession_N256_SHA1. BigInteger values are communicated as
 * hex strings. Hashing is done as string concat of hex numbers. Does not
 * include any session timeout logic on the assumption that can be handled by
 * web server session logic.
 * <p>
 * Specification RFC 2945.
 * 
 * @author Simon Massey
 */
public class SRP6JavascriptServerSessionSHA1 extends SRP6JavascriptServerSession {

	/**
	 * This must match the expected character length of the specified algorithm
	 * i.e. SHA-1 is 40
	 */
	public static int HASH_HEX_LENGTH = 40;

	/**
	 * Create a SHA1 server session compatible with a JavaScript client session.
	 * 
	 * You can generate your own with openssl see {@link OpenSSLCryptoConfig}
	 * 
	 * @param N
	 *            The large safe prime in radix10
	 * @param g
	 *            The safe prime generator in radix10
	 */
	public SRP6JavascriptServerSessionSHA1(String N, String g) {
		super(new SRP6CryptoParams(fromDecimal(N), fromDecimal(g), "SHA-1"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String step1(final String username, final String salt, final String v) {
		BigInteger B = session.step1(username, fromHex(salt), fromHex(v));
		return toHex(B);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String step2(final String A, final String M1) throws Exception {
		BigInteger M2 = session.step2(fromHex(A), fromHex(M1));
		String M2str = toHex(M2);
		M2str = HexHashedRoutines.leadingZerosPad(M2str, HASH_HEX_LENGTH);
		return M2str;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String k() {
		return toHex(SRP6Routines.computeK(config.getMessageDigestInstance(), config.N, config.g));
	}

	/**
	 * Outputs the configuration in the way which can be used to configure
	 * JavaScript.
	 * 
	 * Note that 'k' is fixed but uses the byte array constructor of BigInteger
	 * which is not available in JavaScript to you must set it as configuration.
	 * 
	 * @return Parameters required by JavaScript client.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("g: %s\n", config.g.toString(10)));
		builder.append(String.format("N: %s\n", config.N.toString(10)));
		builder.append(String.format("k: %s\n", k()));
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getState() {
		return session.getState().name();
	}
}
