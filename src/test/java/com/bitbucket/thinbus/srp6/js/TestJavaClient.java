package com.bitbucket.thinbus.srp6.js;

import static com.nimbusds.srp6.BigIntegerUtils.fromHex;
import static com.nimbusds.srp6.BigIntegerUtils.toHex;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.junit.Assert;
import org.junit.Test;

import com.nimbusds.srp6.SRP6ClientCredentials;

public class TestJavaClient {

	final String N_base10 = "19502997308733555461855666625958719160994364695757801883048536560804281608617712589335141535572898798222757219122180598766018632900275026915053180353164617230434226106273953899391119864257302295174320915476500215995601482640160424279800690785793808960633891416021244925484141974964367107";
	final String g_base10 = "2";

	final String username = "tom@arcot.com";
	final String password = "password1234";

	@Test
	public void testJavaX() throws Exception {
		// given
		final BigInteger salt = fromHex("522af15569421614823e502f157f3f856355e63e");
		final HexHashedXRoutine xRoutine = new HexHashedXRoutine(new BigInteger(N_base10));

		// when
		final BigInteger X = xRoutine.computeX(MessageDigest
				.getInstance(SRP6JavascriptServerSessionSHA1.SHA_1), salt
				.toByteArray(), username
				.getBytes(HexHashedRoutines.utf8), password
				.getBytes(HexHashedRoutines.utf8));
		
		// then
		final String x = X.toString(10);
		assertEquals(x, "910785550241352830256417432647532406711731036773");
	}

	@Test
	public void testMutualAuthenticationSHA1() throws Exception {
		SRP6JavaClientSessionSHA1 client = new SRP6JavaClientSessionSHA1(
				N_base10, g_base10);

		String salt = client
				.generateRandomSalt(SRP6JavascriptServerSessionSHA1.HASH_BYTE_LENGTH);

		HexHashedVerifierGenerator generator = new HexHashedVerifierGenerator(
				N_base10, g_base10, SRP6JavascriptServerSessionSHA1.SHA_1);

		String v = generator.generateVerifier(salt, username, password);

		client.step1(username, password);

		SRP6JavascriptServerSession server = new SRP6JavascriptServerSessionSHA1(
				N_base10, g_base10);

		String B = server.step1(username, salt, v);

		SRP6ClientCredentials credentials = client.step2(salt, B);

		String M2 = server.step2(toHex(credentials.A), toHex(credentials.M1));
		
		client.step3(M2);

		// both share a strong session key.
		String cS = client.getSessionKey(false);
		String sS = server.getSessionKey(false);
		Assert.assertEquals(cS, sS);

		// the hash value may be more useful as a secret key.
		String cK = client.getSessionKey(true);
		String sK = server.getSessionKey(true);
		Assert.assertEquals(cK, sK);
	}

	@Test
	public void testMutualAuthenticationSHA256() throws Exception {
		SRP6JavaClientSessionSHA256 client = new SRP6JavaClientSessionSHA256(
				N_base10, g_base10);

		String salt = client
				.generateRandomSalt(SRP6JavascriptServerSessionSHA256.HASH_BYTE_LENGTH);

		HexHashedVerifierGenerator generator = new HexHashedVerifierGenerator(
				N_base10, g_base10, SRP6JavascriptServerSessionSHA256.SHA_256);

		String v = generator.generateVerifier(salt, username, password);

		client.step1(username, password);

		SRP6JavascriptServerSession server = new SRP6JavascriptServerSessionSHA256(
				N_base10, g_base10);

		String B = server.step1(username, salt, v);

		SRP6ClientCredentials credentials = client.step2(salt, B);

		String M2 = server.step2(toHex(credentials.A), toHex(credentials.M1));

		client.step3(M2);
		
		// both share a strong session key.
		String cS = client.getSessionKey(false);
		String sS = server.getSessionKey(false);
		Assert.assertEquals(cS, sS);
		
		// the hash value may be more useful as a secret key. 
		String cK = client.getSessionKey(true);
		String sK = server.getSessionKey(true);
		Assert.assertEquals(cK, sK);
	}

	@Test
	public void testServerSessionSerialization() throws Exception {
		SRP6JavaClientSessionSHA256 client = new SRP6JavaClientSessionSHA256(
				N_base10, g_base10);

		String salt = client
				.generateRandomSalt(SRP6JavascriptServerSessionSHA256.HASH_HEX_LENGTH);

		HexHashedVerifierGenerator generator = new HexHashedVerifierGenerator(
				N_base10, g_base10, SRP6JavascriptServerSessionSHA256.SHA_256);

		String v = generator.generateVerifier(salt, username, password);

		client.step1(username, password);

		SRP6JavascriptServerSession server = new SRP6JavascriptServerSessionSHA256(
				N_base10, g_base10);

		String B = server.step1(username, salt, v);

		// Simulate session serialization
		byte[] serializedSession = toByteArray(server);
		// Creates a copy of the session from a serialized byte array
		SRP6JavascriptServerSession serverCopy = fromByteArray(serializedSession);
		
		SRP6ClientCredentials credentials = client.step2(salt, B);

		String M2 = serverCopy.step2(toHex(credentials.A), toHex(credentials.M1));

		client.step3(M2);
		
		// both share a strong session key.
		String cS = client.getSessionKey(false);
		String sS = serverCopy.getSessionKey(false);
		Assert.assertEquals(cS, sS);
		
		// the hash value may be more useful as a secret key. 
		String cK = client.getSessionKey(true);
		String sK = serverCopy.getSessionKey(true);
		Assert.assertEquals(cK, sK);
	}
	

	//	
	//	 Use a ObjectOutputStream to perform serialization
	//	 
	protected byte[] toByteArray(SRP6JavascriptServerSession session) throws IOException {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream byteStream = null;
		try {
			byteStream = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(byteStream);
			oos.writeObject(session);
			oos.flush();
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (oos != null) {
					oos.flush();
					oos.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			try {
				if (byteStream != null) {
					byteStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	//
	//	  Use a ObjectInputStream to perform deserialization
	//	 
	protected SRP6JavascriptServerSession fromByteArray(byte[] bytes) throws Exception  {
		ObjectInputStream ois = null;
		ByteArrayInputStream byteStream = null;
		try {
			byteStream = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(byteStream);
			SRP6JavascriptServerSession result = (SRP6JavascriptServerSession) ois.readObject();
			return result;
		} catch (IOException | ClassNotFoundException e) {
			throw e;
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			try {
				if (byteStream != null) {
					byteStream.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	// this is a brute force comparison that javascript and Java can login
	// against each other
	public static void main(String[] args) throws Exception {
		TestJavaClient ct = new TestJavaClient();
		System.out.println("SHA1:");
		for (int i = 0; i < 10; i++) {
			long start = System.currentTimeMillis();
			for (int j = 0; j < 1000; j++) {
				ct.testMutualAuthenticationSHA1();
			}
			long end = System.currentTimeMillis();
			System.out.print(" " + (end - start) / 1000);
		}
		System.out.println("\nSHA256:");
		for (int i = 0; i < 10; i++) {
			long start = System.currentTimeMillis();
			for (int j = 0; j < 1000; j++) {
				ct.testMutualAuthenticationSHA256();
			}
			long end = System.currentTimeMillis();
			System.out.print(" " + (end - start) / 1000);
		}
	}
}
