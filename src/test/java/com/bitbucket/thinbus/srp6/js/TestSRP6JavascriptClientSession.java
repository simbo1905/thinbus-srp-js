package com.bitbucket.thinbus.srp6.js;

import org.bitbucket.thinbus.junitjs.JSRunner;
import org.bitbucket.thinbus.junitjs.Tests;
import org.junit.runner.RunWith;


import com.nimbusds.srp6.SRP6CryptoParams;

@Tests({
 "TestSRP6JavascriptClient.js"
})
@RunWith(JSRunner.class)
public class TestSRP6JavascriptClientSession {
	
	public static void main(String[] args) {
		SRP6JavascriptServerSession server1024 = new SRP6JavascriptServerSessionSHA256(
				SRP6CryptoParams.N_1024.toString(10), "2");
		System.out.println("RFC 5054 1024bit:");
		System.out.println(server1024);

		SRP6JavascriptServerSession server1536 = new SRP6JavascriptServerSessionSHA256(
				SRP6CryptoParams.N_1536.toString(10), "2");
		System.out.println("RFC 5054 1536bit:");
		System.out.println(server1536);

		SRP6JavascriptServerSession server2048 = new SRP6JavascriptServerSessionSHA256(
				SRP6CryptoParams.N_2048.toString(10), "2");
		System.out.println("RFC 5054 2048bit:");
		System.out.println(server2048);
	}
}
