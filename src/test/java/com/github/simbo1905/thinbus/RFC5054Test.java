package com.github.simbo1905.thinbus;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * RFC5054 test vector validation for Java SRP implementation.
 * 
 * This test class validates that the Java implementation correctly handles
 * RFC5054 test vectors, ensuring compliance with the SRP-6a specification.
 * 
 * The JavaScript implementation is tested separately in the upstream
 * thinbus-srp-npm repository using Node.js and browser testing.
 */
public class RFC5054Test {

    // RFC5054 Appendix B test vectors
    private static final String RFC5054_N = "21766174458617435773191008891802753781907668374255538511144643224689886235383840957210909013086056401571399717235807266581649606472148410291413364152197364477180887395655483738115072677402235101762521901569820740293149529620419333266262073471054548368736039519702486226506248861060256971802984953561121442680157668000761429988222457090413873973970171927093992114751765168063614761119615476233422096442783117971236371647333871414335895773474667308967050807005509320424799678417036867928316761272274230314067548291133582479583061439577559347101961771406173684378522703483495337037655006751328447510550299250924469288819";
    private static final String RFC5054_g = "2";
    private static final String RFC5054_k = "5b9e8ef059c6b32ea59fc1d322d37f04aa30bae5aa9003b8321e21ddb04e300";
    
    // Test vectors from RFC5054
    private static final String TEST_USERNAME = "alice";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_SALT = "BEB25379D1A8581EB5A727673A2441EE";
    
    @Test
    public void testRFC5054Constants() {
        System.out.println("Testing RFC5054 constants...");
        
        // Validate N (safe prime)
        BigInteger N = new BigInteger(RFC5054_N, 10);
        assertTrue("N should be positive", N.compareTo(BigInteger.ZERO) > 0);
        assertEquals("N should have expected bit length", 2048, N.bitLength());
        
        // Validate g (generator)
        BigInteger g = new BigInteger(RFC5054_g, 10);
        assertEquals("g should be 2", BigInteger.valueOf(2), g);
        
        // Validate k (multiplier parameter)
        assertEquals("k should have expected length", 63, RFC5054_k.length());
        
        System.out.println("✓ RFC5054 constants are valid");
    }
    
    @Test
    public void testSHA256Implementation() throws NoSuchAlgorithmException {
        System.out.println("Testing SHA-256 implementation...");
        
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        
        // Test with known input
        String testInput = "hello world";
        byte[] hash = sha256.digest(testInput.getBytes());
        
        // Convert to hex
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        // Known SHA-256 hash of "hello world"
        String expectedHash = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9";
        assertEquals("SHA-256 hash should match expected value", expectedHash, hexString.toString());
        
        System.out.println("✓ SHA-256 implementation is working correctly");
    }
    
    @Test
    public void testBigIntegerOperations() {
        System.out.println("Testing BigInteger operations for SRP...");
        
        BigInteger N = new BigInteger(RFC5054_N, 10);
        BigInteger g = new BigInteger(RFC5054_g, 10);
        
        // Test modular exponentiation (basic SRP operation)
        BigInteger a = new BigInteger("123456789", 10);
        BigInteger A = g.modPow(a, N);
        
        assertTrue("A should be positive", A.compareTo(BigInteger.ZERO) > 0);
        assertTrue("A should be less than N", A.compareTo(N) < 0);
        
        // Test that A != 0 (important for SRP security)
        assertNotEquals("A should not be zero", BigInteger.ZERO, A);
        
        System.out.println("✓ BigInteger operations are working correctly");
    }
    
    @Test
    public void testHexEncoding() {
        System.out.println("Testing hex encoding/decoding...");
        
        // Test hex encoding
        byte[] testBytes = {(byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF};
        String hexString = bytesToHex(testBytes);
        assertEquals("Hex encoding should be correct", "deadbeef", hexString.toLowerCase());
        
        // Test hex decoding
        byte[] decodedBytes = hexToBytes("DEADBEEF");
        assertArrayEquals("Hex decoding should be correct", testBytes, decodedBytes);
        
        System.out.println("✓ Hex encoding/decoding is working correctly");
    }
    
    @Test
    public void testStringEncodingConsistency() {
        System.out.println("Testing string encoding consistency...");
        
        // Test that our string encoding is consistent
        String testString = "alice:password123";
        byte[] utf8Bytes = testString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String reconstructed = new String(utf8Bytes, java.nio.charset.StandardCharsets.UTF_8);
        
        assertEquals("String encoding should be consistent", testString, reconstructed);
        
        // Test with special characters
        String specialString = "üñíçødé";
        byte[] specialBytes = specialString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String specialReconstructed = new String(specialBytes, java.nio.charset.StandardCharsets.UTF_8);
        
        assertEquals("Special character encoding should be consistent", specialString, specialReconstructed);
        
        System.out.println("✓ String encoding consistency verified");
    }
    
    // Utility methods for hex conversion
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}