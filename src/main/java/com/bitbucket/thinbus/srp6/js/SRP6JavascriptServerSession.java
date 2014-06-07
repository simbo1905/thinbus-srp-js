package com.bitbucket.thinbus.srp6.js;

import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6ServerSession.State;

public interface SRP6JavascriptServerSession {

	public static String defaultN = "19502997308733555461855666625958719160994364695757801883048536560804281608617712589335141535572898798222757219122180598766018632900275026915053180353164617230434226106273953899391119864257302295174320915476500215995601482640160424279800690785793808960633891416021244925484141974964367107";
	public static String defaultg = "2";

	/**
	 * Increments this SRP-6a authentication session to {@link State#STEP_1}.
	 * 
	 * @param username
	 *            The identity 'I' of the authenticating user. Must not be
	 *            {@code null} or empty.
	 * @param salt
	 *            The password salt 's'. Must not be {@code null}.
	 * @param v
	 *            The password verifier 'v'. Must not be {@code null}.
	 * 
	 * @return The server public value 'B' as hex encoded number.
	 * 
	 * @throws IllegalStateException
	 *             If the mehod is invoked in a state other than
	 *             {@link State#INIT}.
	 */
	public String step1(final String username, final String salt, final String v);

	/**
	 * Increments this SRP-6a authentication session to {@link State#STEP_2}.
	 * 
	 * @param A
	 *            The client public value. Must not be {@code null}.
	 * @param M1
	 *            The client evidence message. Must not be {@code null}.
	 * 
	 * @return The server evidence message 'M2' has hex encoded number with
	 *         leading zero padding to match the 256bit hash length.
	 * 
	 * @throws SRP6Exception
	 *             If the client public value 'A' is invalid or the user
	 *             credentials are invalid.
	 * 
	 * @throws IllegalStateException
	 *             If the mehod is invoked in a state other than
	 *             {@link State#STEP_1}.
	 */
	public String step2(final String A, final String M1) throws Exception;

	public String getState();
}
