package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;

import com.nimbusds.srp6.ClientEvidenceRoutine;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Routines;
import com.nimbusds.srp6.SRP6ServerSession;
import com.nimbusds.srp6.ServerEvidenceRoutine;
import com.nimbusds.srp6.URoutine;

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
public class SRP6JavascriptServerSession_N1024_SHA1 implements SRP6JavascriptServerSession {

	/**
	 * This must match the expected character length of the sepecified algorithm
	 * i.e. SHA-1 is 40
	 */
	public static int HASH_HEX_LENGTH = 40;

	public static SRP6CryptoParams config = SRP6CryptoParams.getInstance(1024, "SHA-1");

	protected SRP6ServerSession session = new SRP6ServerSession(config);

	protected final URoutine hexStringHashedKeysRoutine = new HexHashedURoutine();
	protected final ClientEvidenceRoutine hexStringHashedclientEvidenceRoutine = new HexHashedClientEvidenceRoutine();
	protected final ServerEvidenceRoutine hexStringHashedServerEvidenceRoutine = new HexHashedServerEvidenceRoutine();

	public SRP6JavascriptServerSession_N1024_SHA1() {
		session.setHashedKeysRoutine(hexStringHashedKeysRoutine);
		session.setClientEvidenceRoutine(hexStringHashedclientEvidenceRoutine);
		session.setServerEvidenceRoutine(hexStringHashedServerEvidenceRoutine);
	}


	public String step1(final String username, final String salt, final String v) {
		BigInteger B = session.step1(username, fromHex(salt), fromHex(v));
		return toHex(B);
	}

	public String step2(final String A, final String M1) throws Exception {
		BigInteger M2 = session.step2(fromHex(A), fromHex(M1));
		String M2str = toHex(M2);
		M2str = HexHashedRoutines.leadingZerosPad(M2str, HASH_HEX_LENGTH);
		return M2str;
	}

	/**
	 * k is actually fixed and done with hash padding routine so passed from the
	 * server than recomputed in every javascript client.
	 */
	public static String k = toHex(SRP6Routines.computeK(config.getMessageDigestInstance(), config.N, config.g));

	/**
	 * Outputs config for the client scripts.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(String.format("g: %s", config.g.toString(10)));
		System.out.println(String.format("N: %s", config.N.toString(10)));
		System.out.println(String.format("k: %s", k));

		BigInteger value = HexHashedRoutines
				.hashValues(
						config.getMessageDigestInstance(),
						"caebd81efad6debf9cd6128dffd66c6ad07666ce09d518c60c5ebf4fcb042b07462e390d7b52d2a4c32d2cba568a2c56b0ab919c9f4f7c79518073fabd8bd345d761c7655d227f8685ac9c065ab86929978fda40b95f5655bfdb436a75292364");

		String raw = value.toString(16);

		System.out.println("value: " + HexHashedRoutines.leadingZerosPad(raw, HASH_HEX_LENGTH));
	}

	@Override
	public String getState() {
		return session.getState().name();
	}
}
