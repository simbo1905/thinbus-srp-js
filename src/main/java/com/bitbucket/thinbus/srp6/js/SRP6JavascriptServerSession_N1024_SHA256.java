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
 * SRP6JavascriptClientSession_N1024_SHA256. BigInteger values are communicated
 * as hex strings. Hashing is done as string concat of hex numbers. Does not
 * include any session timeout logic on the assumption that can be handled by
 * web server session logic.
 * <p>
 * Specification RFC 2945.
 * 
 * @author Simon Massey
 */
public class SRP6JavascriptServerSession_N1024_SHA256 implements SRP6JavascriptServerSession {

	public static int HASH_HEX_LENGTH = 64;

	public static SRP6CryptoParams config = SRP6CryptoParams.getInstance(1024, "SHA-256");

	protected SRP6ServerSession session = new SRP6ServerSession(config);

	protected final URoutine hexStringHashedKeysRoutine = new HexHashedURoutine();
	protected final ClientEvidenceRoutine hexStringHashedclientEvidenceRoutine = new HexHashedClientEvidenceRoutine();
	protected final ServerEvidenceRoutine hexStringHashedServerEvidenceRoutine = new HexHashedServerEvidenceRoutine();

	public SRP6JavascriptServerSession_N1024_SHA256() {
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
		System.out.println(String.format("g: %s", config.g.toString(16)));
		System.out.println(String.format("N: %s", config.N.toString(16)));
		System.out.println(String.format("k: %s", k));
	}

	@Override
	public String getState() {
		return session.getState().name();
	}
}
