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

package com.nimbusds.openid.connect.sdk.federation.entities;


import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import net.minidev.json.JSONObject;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.util.DateUtils;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.client.ClientMetadata;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;


public class EntityStatementClaimsSetTest extends TestCase {
	
	
	private static final JWKSet JWK_SET;
	
	
	static {
		try {
			RSAKey rsaJWK = new RSAKeyGenerator(2048)
				.keyIDFromThumbprint(true)
				.keyUse(KeyUse.SIGNATURE)
				.generate();
			JWK_SET = new JWKSet(rsaJWK.toPublicJWK());
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void testMinimal()
		throws Exception {
		
		Issuer iss = new Issuer("https://abc-federation.c2id.com");
		Subject sub = new Subject("https://op.c2id.com");
		
		Date iat = DateUtils.fromSecondsSinceEpoch(1000);
		Date exp = DateUtils.fromSecondsSinceEpoch(2000);
		
		// Test ID and EntityID constructors
		for (EntityStatementClaimsSet stmt: Arrays.asList(
			new EntityStatementClaimsSet(
				iss,
				sub,
				iat,
				exp,
				JWK_SET),
			new EntityStatementClaimsSet(
				new EntityID(iss.getValue()),
				new EntityID(sub.getValue()),
				iat,
				exp,
				JWK_SET))) {
			
			stmt.validateRequiredClaimsPresence();
			assertFalse(stmt.isSelfStatement());
			assertFalse(stmt.hasMetadata());
			
			assertEquals(iss, stmt.getIssuer());
			assertEquals(iss.getValue(), stmt.getIssuerEntityID().getValue());
			assertEquals(sub, stmt.getSubject());
			assertEquals(sub.getValue(), stmt.getSubjectEntityID().getValue());
			assertEquals(iat, stmt.getIssueTime());
			assertEquals(exp, stmt.getExpirationTime());
			assertEquals(JWK_SET.toJSONObject(), stmt.getJWKSet().toJSONObject());
			
			assertNull(stmt.getAudience());
			assertNull(stmt.getAuthorityHints());
			assertNull(stmt.getRPMetadata());
			assertNull(stmt.getOPMetadata());
			assertNull(stmt.getOAuthClientMetadata());
			assertNull(stmt.getASMetadata());
			assertNull(stmt.getFederationEntityMetadata());
			assertNull(stmt.getMetadataPolicyJSONObject());
			assertNull(stmt.getConstraints());
			assertNull(stmt.getCriticalExtensionClaims());
			assertNull(stmt.getCriticalPolicyExtensions());
			
			JWTClaimsSet jwtClaimsSet = stmt.toJWTClaimsSet();
			
			assertEquals(iss.getValue(), jwtClaimsSet.getIssuer());
			assertEquals(sub.getValue(), jwtClaimsSet.getSubject());
			assertEquals(iat, jwtClaimsSet.getIssueTime());
			assertEquals(exp, jwtClaimsSet.getExpirationTime());
			assertEquals(JWK_SET.toJSONObject(), jwtClaimsSet.getJSONObjectClaim("jwks"));
			assertEquals(5, jwtClaimsSet.getClaims().size());
			
			stmt = new EntityStatementClaimsSet(jwtClaimsSet);
			
			stmt.validateRequiredClaimsPresence();
			assertFalse(stmt.isSelfStatement());
			assertFalse(stmt.hasMetadata());
			
			assertEquals(iss, stmt.getIssuer());
			assertEquals(iss.getValue(), stmt.getIssuerEntityID().getValue());
			assertEquals(sub, stmt.getSubject());
			assertEquals(sub.getValue(), stmt.getSubjectEntityID().getValue());
			assertEquals(iat, stmt.getIssueTime());
			assertEquals(exp, stmt.getExpirationTime());
			assertEquals(JWK_SET.toJSONObject(), stmt.getJWKSet().toJSONObject());
			
			assertNull(stmt.getAudience());
			assertNull(stmt.getAuthorityHints());
			assertNull(stmt.getRPMetadata());
			assertNull(stmt.getOPMetadata());
			assertNull(stmt.getOAuthClientMetadata());
			assertNull(stmt.getASMetadata());
			assertNull(stmt.getFederationEntityMetadata());
			assertNull(stmt.getMetadataPolicyJSONObject());
			assertNull(stmt.getConstraints());
			assertNull(stmt.getCriticalExtensionClaims());
			assertNull(stmt.getCriticalPolicyExtensions());
		}
	}
	
	
	private static OIDCClientMetadata createRPMetadata() {
		
		OIDCClientMetadata rpMetadata = new OIDCClientMetadata();
		rpMetadata.setRedirectionURI(URI.create("https://example.com"));
		rpMetadata.applyDefaults();
		return rpMetadata;
	}
	
	
	private static OIDCProviderMetadata createOPMetadata() {
		
		OIDCProviderMetadata opMetadata = new OIDCProviderMetadata(
			new Issuer("https://openid.c2id.com"),
			Arrays.asList(SubjectType.PUBLIC, SubjectType.PAIRWISE),
			URI.create("https://openid.c2id.com/jwks.json")
		);
		opMetadata.applyDefaults();
		return opMetadata;
	}
	
	
	private static ClientMetadata createOAuthClientMetadata() {
		
		ClientMetadata clientMetadata = new ClientMetadata();
		clientMetadata.setRedirectionURI(URI.create("https://example.com"));
		clientMetadata.applyDefaults();
		return clientMetadata;
	}
	
	
	private static AuthorizationServerMetadata createASMetadata() {
		
		AuthorizationServerMetadata asMetadata = new AuthorizationServerMetadata(new Issuer("https://openid.c2id.com"));
		asMetadata.applyDefaults();
		return asMetadata;
	}
	
	
	private static FederationEntityMetadata createFederationEntityMetadata() {
		
		return new FederationEntityMetadata(URI.create("https://federation.c2id.com/api"));
	}
	
	
	private static JSONObject createMetadataPolicy() throws ParseException {
		
		return JSONObjectUtils.parse("{\"response_types\":{\"subset_of\": [\"code\"]}}");
	}
	
	
	public void testWithRPMetadata_selfStated()
		throws Exception {
		
		Issuer iss = new Issuer("https://rp.c2id.com");
		Subject sub = new Subject("https://rp.c2id.com");
		
		Date iat = DateUtils.fromSecondsSinceEpoch(1000);
		Date exp = DateUtils.fromSecondsSinceEpoch(2000);
		
		EntityStatementClaimsSet stmt = new EntityStatementClaimsSet(
			iss,
			sub,
			iat,
			exp,
			JWK_SET);
		
		try {
			stmt.validateRequiredClaimsPresence();
			fail();
		} catch (ParseException e) {
			assertEquals("Missing required metadata claim for self-statement", e.getMessage());
		}
		assertTrue(stmt.isSelfStatement());
		assertFalse(stmt.hasMetadata());
		
		// aud
		List<Audience> audList = new Audience("123").toSingleAudienceList();
		stmt.setAudience(audList);
		assertEquals(audList, stmt.getAudience());
		
		// authority_hints
		List<EntityID> authorityHints = Collections.singletonList(new EntityID("abc123"));
		stmt.setAuthorityHints(authorityHints);
		assertEquals(authorityHints, stmt.getAuthorityHints());
		
		// metadata -> openid_relying_party
		OIDCClientMetadata rpMetadata = createRPMetadata();
		stmt.setRPMetadata(rpMetadata);
		assertEquals(rpMetadata.toJSONObject(), stmt.getRPMetadata().toJSONObject());
		
		// passes now
		assertTrue(stmt.hasMetadata());
		stmt.validateRequiredClaimsPresence();
		
		// metadata_policy
		JSONObject metadataPolicy = createMetadataPolicy();
		stmt.setMetadataPolicyJSONObject(metadataPolicy);
		assertEquals(metadataPolicy, stmt.getMetadataPolicyJSONObject());
		
		// constraints
		TrustChainConstraints constraints = new TrustChainConstraints(10, null, null);
		stmt.setConstraints(constraints);
		assertEquals(constraints, stmt.getConstraints());
		
		// crit
		List<String> crit = Collections.singletonList("jti");
		stmt.setCriticalExtensionClaims(crit);
		assertEquals(crit, stmt.getCriticalExtensionClaims());
		
		try {
			stmt.validateRequiredClaimsPresence();
			fail();
		} catch (ParseException e) {
			assertEquals("Missing critical jti claim", e.getMessage());
		}
		
		// jti
		stmt.setClaim("jti", "be0Chi8U");
		
		stmt.validateRequiredClaimsPresence();
		
		// policy_language_crit
		List<String> policyCrit = Collections.singletonList("regexp");
		stmt.setCriticalPolicyExtensions(policyCrit);
		assertEquals(policyCrit, stmt.getCriticalPolicyExtensions());
		
		// output
		JWTClaimsSet jwtClaimsSet = stmt.toJWTClaimsSet();
		
		assertEquals(iss.getValue(), jwtClaimsSet.getIssuer());
		assertEquals(sub.getValue(), jwtClaimsSet.getSubject());
		assertEquals(iat, jwtClaimsSet.getIssueTime());
		assertEquals(exp, jwtClaimsSet.getExpirationTime());
		assertEquals(JWK_SET.toJSONObject(), jwtClaimsSet.getJSONObjectClaim("jwks"));
		assertEquals(audList.get(0).getValue(), jwtClaimsSet.getAudience().get(0));
		assertEquals(authorityHints.get(0).getValue(), jwtClaimsSet.getStringListClaim("authority_hints").get(0));
		
		JSONObject metadata = jwtClaimsSet.getJSONObjectClaim("metadata");
		OIDCClientMetadata parsedRPMetadata = OIDCClientMetadata.parse(JSONObjectUtils.getJSONObject(metadata, "openid_relying_party"));
		assertEquals(rpMetadata.toJSONObject(), parsedRPMetadata.toJSONObject());
		assertEquals(1, metadata.size());
		
		assertEquals(metadataPolicy, jwtClaimsSet.getJSONObjectClaim("metadata_policy"));
		assertEquals(constraints.toJSONObject().toJSONString(), jwtClaimsSet.getJSONObjectClaim("constraints").toJSONString());
		assertEquals(crit, jwtClaimsSet.getStringListClaim("crit"));
		assertEquals("be0Chi8U", jwtClaimsSet.getJWTID());
		assertEquals(policyCrit, jwtClaimsSet.getStringListClaim("policy_language_crit"));
		
		// parse
		EntityStatementClaimsSet parsed = new EntityStatementClaimsSet(jwtClaimsSet);
		
		assertEquals(stmt.getIssuer(), parsed.getIssuer());
		assertEquals(stmt.getSubject(), parsed.getSubject());
		assertEquals(stmt.getIssueTime(), parsed.getIssueTime());
		assertEquals(stmt.getExpirationTime(), parsed.getExpirationTime());
		assertEquals(stmt.getJWKSet().toJSONObject(), parsed.getJWKSet().toJSONObject());
		assertEquals(stmt.getAudience(), parsed.getAudience());
		assertEquals(stmt.getAuthorityHints(), parsed.getAuthorityHints());
		assertEquals(stmt.getRPMetadata().toJSONObject(), parsed.getRPMetadata().toJSONObject());
		assertEquals(stmt.getMetadataPolicyJSONObject(), parsed.getMetadataPolicyJSONObject());
		assertEquals(stmt.getConstraints(), parsed.getConstraints());
		assertEquals(stmt.getCriticalExtensionClaims(), parsed.getCriticalExtensionClaims());
		assertEquals(stmt.getStringClaim("jti"), parsed.getStringClaim("jti"));
		assertEquals(stmt.getCriticalPolicyExtensions(), parsed.getCriticalPolicyExtensions());
	}
	
	
	public void testWithOPMetadata_selfStated()
		throws Exception {
		
		Issuer iss = new Issuer("https://rp.c2id.com");
		Subject sub = new Subject("https://rp.c2id.com");
		
		Date iat = DateUtils.fromSecondsSinceEpoch(1000);
		Date exp = DateUtils.fromSecondsSinceEpoch(2000);
		
		EntityStatementClaimsSet stmt = new EntityStatementClaimsSet(
			iss,
			sub,
			iat,
			exp,
			JWK_SET);
		
		OIDCProviderMetadata opMetadata = createOPMetadata();
		
		stmt.setOPMetadata(opMetadata);
		assertEquals(opMetadata.toJSONObject(), stmt.getOPMetadata().toJSONObject());
		
		JWTClaimsSet jwtClaimsSet = stmt.toJWTClaimsSet();
		JSONObject metadata = jwtClaimsSet.getJSONObjectClaim("metadata");
		assertEquals(opMetadata.toJSONObject(), JSONObjectUtils.getJSONObject(metadata, "openid_provider"));
		
		stmt = new EntityStatementClaimsSet(jwtClaimsSet);
		assertEquals(opMetadata.toJSONObject(), stmt.getOPMetadata().toJSONObject());
		
		stmt.validateRequiredClaimsPresence();
		
		stmt.setOPMetadata(null);
		assertNull(stmt.getOPMetadata());
	}
	
	
	public void testWithASMetadata_selfStated()
		throws Exception {
		
		Issuer iss = new Issuer("https://rp.c2id.com");
		Subject sub = new Subject("https://rp.c2id.com");
		
		Date iat = DateUtils.fromSecondsSinceEpoch(1000);
		Date exp = DateUtils.fromSecondsSinceEpoch(2000);
		
		EntityStatementClaimsSet stmt = new EntityStatementClaimsSet(
			iss,
			sub,
			iat,
			exp,
			JWK_SET);
		
		AuthorizationServerMetadata asMetadata = createASMetadata();
		
		stmt.setASMetadata(asMetadata);
		assertEquals(asMetadata.toJSONObject(), stmt.getASMetadata().toJSONObject());
		
		JWTClaimsSet jwtClaimsSet = stmt.toJWTClaimsSet();
		JSONObject metadata = jwtClaimsSet.getJSONObjectClaim("metadata");
		assertEquals(asMetadata.toJSONObject(), JSONObjectUtils.getJSONObject(metadata, "oauth_authorization_server"));
		
		stmt = new EntityStatementClaimsSet(jwtClaimsSet);
		assertEquals(asMetadata.toJSONObject(), stmt.getASMetadata().toJSONObject());
		
		stmt.validateRequiredClaimsPresence();
		
		stmt.setASMetadata(null);
		assertNull(stmt.getASMetadata());
	}
	
	
	public void testWithOAuthClientMetadata_selfStated()
		throws Exception {
		
		Issuer iss = new Issuer("https://rp.c2id.com");
		Subject sub = new Subject("https://rp.c2id.com");
		
		Date iat = DateUtils.fromSecondsSinceEpoch(1000);
		Date exp = DateUtils.fromSecondsSinceEpoch(2000);
		
		EntityStatementClaimsSet stmt = new EntityStatementClaimsSet(
			iss,
			sub,
			iat,
			exp,
			JWK_SET);
		
		ClientMetadata clientMetadata = createOAuthClientMetadata();
		
		stmt.setOAuthClientMetadata(clientMetadata);
		assertEquals(clientMetadata.toJSONObject(), stmt.getOAuthClientMetadata().toJSONObject());
		
		JWTClaimsSet jwtClaimsSet = stmt.toJWTClaimsSet();
		JSONObject metadata = jwtClaimsSet.getJSONObjectClaim("metadata");
		assertEquals(clientMetadata.toJSONObject(), JSONObjectUtils.getJSONObject(metadata, "oauth_client"));
		
		stmt = new EntityStatementClaimsSet(jwtClaimsSet);
		assertEquals(clientMetadata.toJSONObject(), stmt.getOAuthClientMetadata().toJSONObject());
		
		stmt.validateRequiredClaimsPresence();
		
		stmt.setOAuthClientMetadata(null);
		assertNull(stmt.getOAuthClientMetadata());
	}
	
	
	public void testWithFederationEntityMetadata_selfStated()
		throws Exception {
		
		Issuer iss = new Issuer("https://fed.c2id.com");
		Subject sub = new Subject("https://fed.c2id.com");
		
		Date iat = DateUtils.fromSecondsSinceEpoch(1000);
		Date exp = DateUtils.fromSecondsSinceEpoch(2000);
		
		EntityStatementClaimsSet stmt = new EntityStatementClaimsSet(
			iss,
			sub,
			iat,
			exp,
			JWK_SET);
		
		FederationEntityMetadata fedMetadata = createFederationEntityMetadata();
		
		stmt.setFederationEntityMetadata(fedMetadata);
		assertEquals(fedMetadata.toJSONObject(), stmt.getFederationEntityMetadata().toJSONObject());
		
		JWTClaimsSet jwtClaimsSet = stmt.toJWTClaimsSet();
		JSONObject metadata = jwtClaimsSet.getJSONObjectClaim("metadata");
		assertEquals(fedMetadata.toJSONObject(), JSONObjectUtils.getJSONObject(metadata, "federation_entity"));
		
		stmt = new EntityStatementClaimsSet(jwtClaimsSet);
		assertEquals(fedMetadata.toJSONObject(), stmt.getFederationEntityMetadata().toJSONObject());
		
		stmt.validateRequiredClaimsPresence();
		
		stmt.setFederationEntityMetadata(null);
		assertNull(stmt.getFederationEntityMetadata());
	}
}
