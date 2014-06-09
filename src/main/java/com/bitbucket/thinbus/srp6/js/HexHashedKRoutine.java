package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;

import com.nimbusds.srp6.KRoutine;
import com.nimbusds.srp6.KRoutineContext;
import com.nimbusds.srp6.SRP6CryptoParams;

/**
 * Custom routine interface for computing the hashed keys 'u'. Compatible with
 * browser implementations by using hashing of string concatenated hex strings.
 * 
 * <p>
 * Specification RFC 2945
 * 
 * @author Simon Massey
 */
final class HexHashedKRoutine implements KRoutine {

	/**
	 * Computes the SRP-6 multiplier k
	 * 
	 * @param cryptoParams
	 *            The crypto parameters for the SRP-6a protocol.
	 * @param ctx
	 *            Snapshot of the SRP-6a client session variables which may be
	 *            used in the computation of the multiplier 'k'.
	 * 
	 * @return The resulting 'k' value as as 'H( HEX(N) | HEX(g) )'.
	 */
	@Override
	public BigInteger computeK(SRP6CryptoParams cryptoParams, KRoutineContext ctx) {
		return HexHashedRoutines.hashValues(cryptoParams.getMessageDigestInstance(), toHex(ctx.N), toHex(ctx.g));
	}

}