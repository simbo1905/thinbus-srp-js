package com.bitbucket.thinbus;

import com.bitbucket.thinbus.srp6.js.HexHashedVerifierGenerator;
import com.bitbucket.thinbus.srp6.js.SRP6JavaClientSessionSHA1;
import com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSession;
import com.bitbucket.thinbus.srp6.js.SRP6JavascriptServerSessionSHA1;
import com.nimbusds.srp6.SRP6ClientCredentials;
import org.junit.Assert;

import static com.nimbusds.srp6.BigIntegerUtils.toHex;

public class Demo {
    final static String N_base10 = "19502997308733555461855666625958719160994364695757801883048536560804281608617712589335141535572898798222757219122180598766018632900275026915053180353164617230434226106273953899391119864257302295174320915476500215995601482640160424279800690785793808960633891416021244925484141974964367107";
    final static String g_base10 = "2";

    final static String username = "tom@arcot.com";
    final static String password = "password1234";

    /**
     * Ths demonstrates how to execute the protocol.
     * Note you should consider using the SHA256 classes.
     * Note you can create a custom N using OpenSSL and the tool OpenSSLCryptoConfigConverter.
     * Note the client verifying the M2 may be optional but is advised.
     * Note using the shared session key is entirely optional but may help against man-in-the-middle attacks as you can
     * use it to HMAC sign the payloads between client and server.
     */
    public static void main(String[] args) throws Exception {

        // ---------------------------------------------------
        //
        // Client Registration. Note logic in this section can
        // be done entirely without any server.
        //

        // We have a client
        final SRP6JavaClientSessionSHA1 client = new SRP6JavaClientSessionSHA1(
                N_base10, g_base10);

        // Once and only once a random salt needs to be generated.
        final String salt = client
                .generateRandomSalt(SRP6JavascriptServerSessionSHA1.HASH_BYTE_LENGTH);

        // The client needs to generate a verifier based on N, g, salt and H (hash).
        final HexHashedVerifierGenerator generator = new HexHashedVerifierGenerator(
                N_base10, g_base10, SRP6JavascriptServerSessionSHA1.SHA_1);

        // The client cooks the verifier. This must be securely registered with the server.
        // Note: The verifier includes the hash of the username. So if the user changes eitehr their password or their
        // username they must generate a new verifier and securely register it with the server.
        final String verifier = generator.generateVerifier(salt, username, password);

        // ---------------------------------------------------
        //
        // Client Authentication
        //

        // The server
        SRP6JavascriptServerSession server = new SRP6JavascriptServerSessionSHA1(
                N_base10, g_base10);


        // The client is initialised with username and password
        client.step1(username, password);

        // The server generates a public challenge B based on the username, salt and verifier.
        // Note: The verifier should have been kept secret.
        String B = server.step1(username, salt, verifier);

        // The server sends the public challenge B to the client.
        // The server can can also send the salt as it is public in the protocol.
        // The client computes a proof-of-password which is the credential.
        SRP6ClientCredentials credentials = client.step2(salt, B);

        // The server uses the client public value A and client proof M1
        // Note: this method will throw if the client proof M1 is bad.
        String M2 = server.step2(toHex(credentials.A), toHex(credentials.M1));

        // Success! The last step did not throw so the client knows the password that
        // created the verifier!
        System.out.println("The server has verified the client knows the password that matches the verifier!");

        // The server can send the M2 to the client to check.
        // Note this will throw an exception if the server does not know the true verifier.
        client.step3(M2);

        // Success! The last step did not throw so the client knows that the server knows the verifier.
        System.out.println("The client has verified the server knows the verifier!");

        // Now both share a strong session key.
        String cS = client.getSessionKey(false);
        String sS = server.getSessionKey(false);
        Assert.assertEquals(cS, sS);

        // The hash value may be more useful as a secret key.
        String cK = client.getSessionKey(true);
        String sK = server.getSessionKey(true);
        Assert.assertEquals(cK, sK);

        System.out.println(String.format("We have a shared session key we can use! %s", sK));
    }
}
