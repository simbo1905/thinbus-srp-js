package com.bitbucket.thinbus.srp6.js;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nimbusds.srp6.SRP6Routines;

public class OpenSSLCryptoConfig {
	public static void main(String[] args) throws Exception {
		System.out.println(String.format("attempting to open a openssl dhparam text file at: %s", args[0]));

		List<String> lines = Files.readAllLines(Paths.get(args[0]), Charset.forName("UTF8"));

		StringBuilder hexparts = new StringBuilder();

		int bits = 0;
		int generator = 0;

		for (String line : lines) {
			if (line.startsWith("Diffie-Hellman-Parameters:")) {
				try {
					bits = bits(line);
				} catch (Exception e) {
					throw new AssertionError("could not parse 'xxxx bit' number out of line beginning 'Diffie-Hellman-Parameters'");
				}
			} else if (line.endsWith("prime:")) {
				// skip this one
			} else if (line.endsWith(":")) {
				hexparts.append(line.trim());
			} else if (line.contains("generator")) {
				try {
					generator = generator(line);
				} catch (Exception e) {
					throw new AssertionError("could not parse 'generator: x' number out of line containing 'generator'");
				}
			}
		}

		if (bits <= 0) {
			throw new AssertionError("could not parse 'xxxx bit' number out of line beginning 'Diffie-Hellman-Parameters'");
		}

		if (generator <= 0) {
			throw new AssertionError("could not parse 'generator: x' number out of line containing 'generator'");
		}

		String primeHex = hexparts.toString().replace(":", "");

		System.out.println("bits:" + bits);

		BigInteger N = new BigInteger(primeHex, 16);
		BigInteger g = new BigInteger(generator + "");

		System.out.println("hashing to create 'k' using " + args[1]);

		MessageDigest digest = MessageDigest.getInstance(args[1]);
		BigInteger k = SRP6Routines.computeK(digest, N, g);

		System.out.println("computing");
		System.out.println("N base10: " + N.toString(10));
		System.out.println("g base10:" + g.toString(10));
		System.out.println("k base16:" + k.toString(16));
	}

	static Pattern generatorPattern = Pattern.compile(".*generator: (\\d*) \\(.*");

	private static int generator(String line) {
		Matcher matcher = generatorPattern.matcher(line);
		matcher.matches();
		String number = matcher.group(1);
		return Integer.valueOf(number);
	}

	static Pattern bitsPattern = Pattern.compile(".*\\((\\d*) bit\\).*");

	private static int bits(String line) {
		Matcher matcher = bitsPattern.matcher(line);
		matcher.matches();
		String number = matcher.group(1);
		return Integer.valueOf(number);
	}

}
