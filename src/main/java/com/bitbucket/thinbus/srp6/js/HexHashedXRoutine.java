package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.nimbusds.srp6.XRoutine;

public class HexHashedXRoutine implements XRoutine {
	/**
	 * Computes the password key 'x'.
	 *
	 * <p>
	 * Tip: To convert a string to a byte array you can use
	 * {@code String.getBytes()} or
	 * {@code String.getBytes(java.nio.charset.Charset)}. To convert a big
	 * integer to a byte array you can use {@code BigInteger.toByteArray()}.
	 *
	 * @param digest
	 *            The hash function 'H'. To enforce a particular hash algorithm,
	 *            e.g. "SHA-1", you may perform a check that throws an
	 *            {@code IllegalArgumentException} or ignore this argument
	 *            altogether.
	 * @param salt
	 *            The salt 's'. This is considered a mandatory argument in
	 *            computation of 'x'.
	 * @param username
	 *            The user identity 'I'. It may be ignored if the username is
	 *            allowed to change or if a user may authenticate with multiple
	 *            alternate identities, such as name and email address.
	 * @param password
	 *            The user password 'P'. This is considered a mandatory argument
	 *            in the computation of 'x'.
	 *
	 * @return The resulting 'x' value.
	 */
	@Override
	public BigInteger computeX(MessageDigest digest, byte[] salt,
			byte[] username, byte[] password) {
		final String i = new String(username, HexHashedRoutines.utf8);
		final String p = new String(password, HexHashedRoutines.utf8);
		final String s = toHex(new BigInteger(1, salt));
		final String x = HexHashedRoutines.hashCredentials(digest, s, i, p);
		final BigInteger X = fromHex(x);
		return X;
	}

}
