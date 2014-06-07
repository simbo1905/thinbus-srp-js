package com.bitbucket.thinbus.srp6.js;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		System.out.println("g:" + generator);
		System.out.println("N:" + primeHex);
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
