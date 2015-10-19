package com.bitbucket.thinbus.srp6.js;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;

public class TestOpenSSLCryptoConfigConverter {
	
	String asString(List<String> collectionOfStrings, String seperator) {
		StringBuilder result = new StringBuilder();
	    for(String string : collectionOfStrings) {
	        result.append(string);
	        result.append(seperator);
	    } 
	    return result.toString();
	}
	
	@Test
	public void testVector1() throws Exception {
		List<String> config = new ArrayList<String>();
		config.add("Diffie-Hellman-Parameters: (256 bit)");
		config.add("    prime:");
		config.add("        00:a0:f8:c5:41:4f:34:4b:39:d6:f5:4f:c9:a1:87:");
		config.add("        31:ec:96:f6:72:46:05:c3:a2:db:8c:3e:95:04:52:");
		config.add("        8e:ec:63");
		config.add("    generator: 2 (0x2)");
		config.add("-----BEGIN DH PARAMETERS-----");
		config.add("MCYCIQCg+MVBTzRLOdb1T8mhhzHslvZyRgXDotuMPpUEUo7sYwIBAg==");
		config.add("-----END DH PARAMETERS-----");
		
		OpenSSLCryptoConfigConverter converter = new OpenSSLCryptoConfigConverter();
		String result = asString(converter.run("SHA-256", config), "|");
		Assert.assertEquals("hashing to create 'k' using SHA-256|computing...|N base10: 4339790061125498774278539860359278960481742923644618609630028575867986|g base10: 2|k base16: 84e72aa48bc2e86b1a9352461ae24ab122a89163d1587a10e333e5241029bcb2|", result);
	}
	
	@Test
	public void testVector2() throws Exception {
		List<String> config = new ArrayList<String>();
		config.add("    PKCS#3 DH Parameters: (2048 bit)");
		config.add("        prime:");
		config.add("            00:c1:9e:d3:46:57:ea:4e:a9:c1:c9:28:49:02:0a:");
		config.add("            88:cc:ba:f0:50:92:69:57:7a:ef:47:95:0a:e6:0f:");
		config.add("            83:68:27:48:8e:cd:e2:10:55:9f:e6:a3:a6:a7:31:");
		config.add("            44:79:d5:e2:31:d1:1d:14:5c:12:cf:d1:ed:5f:14:");
		config.add("            f3:d1:6f:4b:01:7f:38:75:a1:e4:6e:9f:58:97:08:");
		config.add("            dd:9c:8e:93:7f:0c:12:bc:0e:44:b9:83:d3:5e:ba:");
		config.add("            e3:75:2a:2f:03:a3:86:a8:13:c1:b2:20:a3:cf:e9:");
		config.add("            7e:fe:68:ea:a2:bf:22:97:1d:22:c7:5f:9a:94:d9:");
		config.add("            5c:49:34:e1:9e:fa:60:45:09:5a:ab:ee:64:19:8f:");
		config.add("            22:82:b8:bb:44:44:a7:0b:ff:f5:3b:fd:fb:67:2b:");
		config.add("            cc:ac:24:7f:08:99:77:4f:78:c7:03:80:47:ee:9c:");
		config.add("            73:e3:68:1d:70:cd:d9:91:99:58:27:2f:07:3d:a6:");
		config.add("            75:10:9d:13:2e:6e:e0:4f:51:1d:15:c9:0d:2b:66:");
		config.add("            92:fe:47:f2:0b:14:f0:42:fd:74:79:8b:65:19:76:");
		config.add("            2c:70:79:1d:50:23:40:50:54:72:9a:a1:f9:fa:8a:");
		config.add("            85:70:93:5e:f4:0a:84:df:f0:dc:ea:db:75:5e:da:");
		config.add("            48:b9:07:68:ce:05:cc:11:e3:cf:5b:96:cd:9e:ac:");
		config.add("            3e:db");
		config.add("        generator: 2 (0x2)");
		config.add("-----BEGIN DH PARAMETERS-----");
		config.add("MIIBCAKCAQEAwZ7TRlfqTqnByShJAgqIzLrwUJJpV3rvR5UK5g+DaCdIjs3iEFWf");
		config.add("5qOmpzFEedXiMdEdFFwSz9HtXxTz0W9LAX84daHkbp9YlwjdnI6TfwwSvA5EuYPT");
		config.add("XrrjdSovA6OGqBPBsiCjz+l+/mjqor8ilx0ix1+alNlcSTThnvpgRQlaq+5kGY8i");
		config.add("gri7RESnC//1O/37ZyvMrCR/CJl3T3jHA4BH7pxz42gdcM3ZkZlYJy8HPaZ1EJ0T");
		config.add("Lm7gT1EdFckNK2aS/kfyCxTwQv10eYtlGXYscHkdUCNAUFRymqH5+oqFcJNe9AqE");
		config.add("3/Dc6tt1XtpIuQdozgXMEePPW5bNnqw+2wIBAg==");
		config.add("-----END DH PARAMETERS-----");
		OpenSSLCryptoConfigConverter converter = new OpenSSLCryptoConfigConverter();
		String result = asString(converter.run("SHA-256", config), "|");
		Assert.assertEquals( "hashing to create 'k' using SHA-256|computing...|N base10: 372960091079720751989909048554294306149886346283860870431573799239069610415954244666235106287601071930638050431093114342248735293110907005276114476650994559344298794510644298732503592260847033856769232229699252939435469131323168558999091150292285953782460002852378666308968605969687404152854768671064411461845333374551797538688879624205302711220429860194106657616427072745163968967949330179669690872857391834663454091842163036536581702548821499297003562510044866394037150781980707208808437113514462813794244140458383061981232208730069630371760365712907321687567494139770845215898711850838388862296886648452783788|g base10: 2|k base16: 1b9ae7e8271cc873aadd850f220e6d398df93e54cb6b6a717b1e76e77b0adc60|", result);
	}
}
