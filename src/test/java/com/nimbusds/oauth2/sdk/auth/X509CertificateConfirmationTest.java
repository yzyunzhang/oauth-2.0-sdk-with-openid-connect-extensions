/*
 * oauth2-oidc-sdk
 *
 * Copyright 2012-2016, Connect2id Ltd and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.nimbusds.oauth2.sdk.auth;


import java.security.cert.X509Certificate;

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.ByteUtils;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import junit.framework.TestCase;
import net.minidev.json.JSONObject;


public class X509CertificateConfirmationTest extends TestCase {
	
	
	public void testLifeCycle()
		throws Exception {
		
		String pemCert = "-----BEGIN CERTIFICATE-----" +
			"MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL" +
			"MAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC" +
			"VU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx" +
			"NDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD" +
			"TjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu" +
			"ZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j" +
			"V14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj" +
			"gbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA" +
			"FFXI70krXeQDxZgbaCQoR4jUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE" +
			"CBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS" +
			"BgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE" +
			"BQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju" +
			"Wm7DCfrPNGVwFWUQOmsPue9rZBgO" +
			"-----END CERTIFICATE-----";
		
		X509Certificate clientCert = X509CertUtils.parse(pemCert);
		
		Base64URL x5t = X509CertUtils.computeSHA256Thumbprint(clientCert);
		assertEquals(256, ByteUtils.bitLength(x5t.decode()));
		
		X509CertificateConfirmation certCnf = new X509CertificateConfirmation(x5t);
		
		assertEquals(x5t, certCnf.getValue());
		
		JSONObject jsonObject = certCnf.toJSONObject();
		JSONObject cnfObject = JSONObjectUtils.getJSONObject(jsonObject, "cnf");
		assertEquals(x5t.toString(), JSONObjectUtils.getString(cnfObject, "x5t#S256"));
		assertEquals(1, cnfObject.size());
		assertEquals(1, jsonObject.size());
		
		certCnf = X509CertificateConfirmation.parse(JWTClaimsSet.parse(jsonObject));
		assertEquals(x5t, certCnf.getValue());
	}
	
	
	public void testRejectNullArg() {
		
		try {
			new X509CertificateConfirmation(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The X.509 certificate thumbprint must not be null", e.getMessage());
		}
	}
}
