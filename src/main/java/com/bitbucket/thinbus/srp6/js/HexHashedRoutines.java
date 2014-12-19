package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.toHex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Secure Remote Password (SRP-6a) hashing routine for Java compatible with
 * browser implementations by using hashing of string concatenated hex strings.
 * 
 * <p>
 * Specification RFC 2945
 * 
 * @author Simon Massey
 */
public class HexHashedRoutines {
	
	public final static Charset utf8 = utf8();

	static Charset utf8() {
		return Charset.forName("UTF8");
	}

	public static BigInteger hashValues(final MessageDigest digest, final String... values) {
		final StringBuilder builder = new StringBuilder();
		for (String v : values) {
			builder.append(v);
		}
		final byte[] bytes = builder.toString().getBytes(utf8);
		digest.update(bytes, 0, bytes.length);
		return new BigInteger(1, digest.digest());
	}

	private HexHashedRoutines() {
		// empty
	}

	public static String leadingZerosPad(String value, int desiredLength) {
		StringBuilder builder = new StringBuilder();
		int difference = desiredLength - value.length();
		for (int i = 0; i < difference; i++) {
			builder.append('0');
		}
		builder.append(value);
		return builder.toString();
	}

	public static String hashCredentials(MessageDigest digest, String salt,
			String identity, String password) {
		digest.reset();

		String concat = identity + ":" + password;

		digest.update(concat.getBytes(utf8));
		byte[] output = digest.digest();
		digest.reset();

		final String hash1 = toHex(new BigInteger(1, output));
		concat = (salt + hash1).toUpperCase();

		digest.update(concat.getBytes(utf8));
		output = digest.digest();

		return toHexString(output);
		// return toHex(new BigInteger(1, output));
	}

	/**
	 * Lower case Hex Digits.
	 */
	static final String HEX_DIGITS = "0123456789abcdef";

	/**
	 * Byte mask.
	 */
	static final int BYTE_MSK = 0xFF;

	/**
	 * Hex digit mask.
	 */
	static final int HEX_DIGIT_MASK = 0xF;

	/**
	 * Number of bits per Hex digit (4).
	 */
	static final int HEX_DIGIT_BITS = 4;

	/**
	 * https://raw.githubusercontent.com/stivlo/obliquid-lib/master/src/main/
	 * java/org/obliquid/helpers/StringHelper.java
	 * 
	 * Compute a String in HexDigit from the input.
	 * 
	 * @param byteArray
	 *            a row byte array
	 * @return a hex String
	 */
	public static String toHexString(final byte[] byteArray) {
		StringBuilder sb = new StringBuilder(byteArray.length * 2);
		for (int i = 0; i < byteArray.length; i++) {
			int b = byteArray[i] & BYTE_MSK;
			sb.append(HEX_DIGITS.charAt(b >>> HEX_DIGIT_BITS)).append(
					HEX_DIGITS.charAt(b & HEX_DIGIT_MASK));
		}
		return sb.toString();
	}

}
