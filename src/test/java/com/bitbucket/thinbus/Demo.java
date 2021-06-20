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

    public static void main(String[] args) throws Exception {
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

        // this method will throw if the client proof "A" is bad
        String M2 = server.step2(toHex(credentials.A), toHex(credentials.M1));

        System.out.println("The server has verified the client knows the password that matches the verifier!");

        client.step3(M2);

        System.out.println("The client has verified the server knows the verifier!");

        // both share a strong session key.
        String cS = client.getSessionKey(false);
        String sS = server.getSessionKey(false);
        Assert.assertEquals(cS, sS);

        // the hash value may be more useful as a secret key.
        String cK = client.getSessionKey(true);
        String sK = server.getSessionKey(true);
        Assert.assertEquals(cK, sK);

        System.out.println(String.format("We have a shared session key we can use! %s", sK));
    }
}
