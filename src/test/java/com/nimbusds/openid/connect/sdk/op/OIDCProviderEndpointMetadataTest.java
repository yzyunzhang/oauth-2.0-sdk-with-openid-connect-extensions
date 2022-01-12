/*
 * oauth2-oidc-sdk
 *
 * Copyright 2012-2021, Connect2id Ltd and contributors.
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

package com.nimbusds.openid.connect.sdk.op;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import junit.framework.TestCase;
import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerEndpointMetadata;


public class OIDCProviderEndpointMetadataTest extends TestCase {
	
	
	public void testRegisteredParameters() {
		
		Set<String> paramNames = OIDCProviderEndpointMetadata.getRegisteredParameterNames();
		
		// OAuth
		assertTrue(paramNames.contains("authorization_endpoint"));
		assertTrue(paramNames.contains("token_endpoint"));
		assertTrue(paramNames.contains("registration_endpoint"));
		assertTrue(paramNames.contains("pushed_authorization_request_endpoint"));
		assertTrue(paramNames.contains("request_object_endpoint"));
		assertTrue(paramNames.contains("introspection_endpoint"));
		assertTrue(paramNames.contains("revocation_endpoint"));
		assertTrue(paramNames.contains("device_authorization_endpoint"));
		assertTrue(paramNames.contains("backchannel_authentication_endpoint"));
		
		// OIDC
		assertTrue(paramNames.contains("userinfo_endpoint"));
		assertTrue(paramNames.contains("check_session_iframe"));
		assertTrue(paramNames.contains("end_session_endpoint"));
		assertTrue(paramNames.contains("federation_registration_endpoint"));
		assertEquals(13, paramNames.size());
	}


	public void testEmpty() throws ParseException {
		
		OIDCProviderEndpointMetadata endpointMetadata = new OIDCProviderEndpointMetadata();
		
		// OAuth
		assertNull(endpointMetadata.getAuthorizationEndpointURI());
		assertNull(endpointMetadata.getTokenEndpointURI());
		assertNull(endpointMetadata.getRegistrationEndpointURI());
		assertNull(endpointMetadata.getPushedAuthorizationRequestEndpointURI());
		assertNull(endpointMetadata.getRequestObjectEndpoint());
		assertNull(endpointMetadata.getIntrospectionEndpointURI());
		assertNull(endpointMetadata.getRevocationEndpointURI());
		assertNull(endpointMetadata.getDeviceAuthorizationEndpointURI());
		assertNull(endpointMetadata.getBackChannelAuthenticationEndpoint());
		
		// OIDC
		assertNull(endpointMetadata.getUserInfoEndpointURI());
		assertNull(endpointMetadata.getCheckSessionIframeURI());
		assertNull(endpointMetadata.getEndSessionEndpointURI());
		assertNull(endpointMetadata.getFederationRegistrationEndpointURI());
		
		JSONObject jsonObject = endpointMetadata.toJSONObject();
		
		ReadOnlyOIDCProviderEndpointMetadata parsedEndpointMetadata = OIDCProviderEndpointMetadata.parse(jsonObject);
		
		// OAuth
		assertNull(parsedEndpointMetadata.getAuthorizationEndpointURI());
		assertNull(parsedEndpointMetadata.getTokenEndpointURI());
		assertNull(parsedEndpointMetadata.getRegistrationEndpointURI());
		assertNull(parsedEndpointMetadata.getPushedAuthorizationRequestEndpointURI());
		assertNull(parsedEndpointMetadata.getRequestObjectEndpoint());
		assertNull(parsedEndpointMetadata.getIntrospectionEndpointURI());
		assertNull(parsedEndpointMetadata.getRevocationEndpointURI());
		assertNull(parsedEndpointMetadata.getDeviceAuthorizationEndpointURI());
		assertNull(parsedEndpointMetadata.getBackChannelAuthenticationEndpoint());
		
		// OIDC
		assertNull(parsedEndpointMetadata.getUserInfoEndpointURI());
		assertNull(parsedEndpointMetadata.getCheckSessionIframeURI());
		assertNull(parsedEndpointMetadata.getEndSessionEndpointURI());
		assertNull(parsedEndpointMetadata.getFederationRegistrationEndpointURI());
	}
	
	
	public void testGetterAndSetters() throws ParseException, URISyntaxException {
		
		OIDCProviderEndpointMetadata endpointMetadata = new OIDCProviderEndpointMetadata();
		
		endpointMetadata.setAuthorizationEndpointURI(new URI("https://c2id.com/authz"));
		assertEquals(new URI("https://c2id.com/authz"), endpointMetadata.getAuthorizationEndpointURI());
		
		endpointMetadata.setTokenEndpointURI(new URI("https://c2id.com/token"));
		assertEquals(new URI("https://c2id.com/token"), endpointMetadata.getTokenEndpointURI());
		
		endpointMetadata.setRegistrationEndpointURI(new URI("https://c2id.com/reg"));
		assertEquals(new URI("https://c2id.com/reg"), endpointMetadata.getRegistrationEndpointURI());
		
		endpointMetadata.setIntrospectionEndpointURI(new URI("https://c2id.com/inspect"));
		assertEquals(new URI("https://c2id.com/inspect"), endpointMetadata.getIntrospectionEndpointURI());
		
		endpointMetadata.setRevocationEndpointURI(new URI("https://c2id.com/revoke"));
		assertEquals(new URI("https://c2id.com/revoke"), endpointMetadata.getRevocationEndpointURI());
		
		endpointMetadata.setPushedAuthorizationRequestEndpointURI(new URI("https://c2id.com/par"));
		assertEquals(new URI("https://c2id.com/par"), endpointMetadata.getPushedAuthorizationRequestEndpointURI());
		
		endpointMetadata.setRequestObjectEndpoint(new URI("https://c2id.com/jar"));
		assertEquals(new URI("https://c2id.com/jar"), endpointMetadata.getRequestObjectEndpoint());
		
		endpointMetadata.setDeviceAuthorizationEndpointURI(new URI("https://c2id.com/device"));
		assertEquals(new URI("https://c2id.com/device"), endpointMetadata.getDeviceAuthorizationEndpointURI());
		
		endpointMetadata.setBackChannelAuthenticationEndpoint(new URI("https://c2id.com/ciba"));
		assertEquals(new URI("https://c2id.com/ciba"), endpointMetadata.getBackChannelAuthenticationEndpoint());
		
		endpointMetadata.setUserInfoEndpointURI(new URI("https://c2id.com/userinfo"));
		assertEquals(new URI("https://c2id.com/userinfo"), endpointMetadata.getUserInfoEndpointURI());
		
		endpointMetadata.setCheckSessionIframeURI(new URI("https://c2id.com/session"));
		assertEquals(new URI("https://c2id.com/session"), endpointMetadata.getCheckSessionIframeURI());
		
		endpointMetadata.setEndSessionEndpointURI(new URI("https://c2id.com/logout"));
		assertEquals(new URI("https://c2id.com/logout"), endpointMetadata.getEndSessionEndpointURI());
		
		endpointMetadata.setFederationRegistrationEndpointURI(new URI("https://c2id.com/fed"));
		assertEquals(new URI("https://c2id.com/fed"), endpointMetadata.getFederationRegistrationEndpointURI());
		
		JSONObject jsonObject = endpointMetadata.toJSONObject();
		
		for (String paramName: OIDCProviderEndpointMetadata.getRegisteredParameterNames()) {
			assertTrue(paramName, jsonObject.containsKey(paramName));
		}
		
		endpointMetadata = OIDCProviderEndpointMetadata.parse(jsonObject);
		
		assertEquals(new URI("https://c2id.com/authz"), endpointMetadata.getAuthorizationEndpointURI());
		assertEquals(new URI("https://c2id.com/token"), endpointMetadata.getTokenEndpointURI());
		assertEquals(new URI("https://c2id.com/reg"), endpointMetadata.getRegistrationEndpointURI());
		assertEquals(new URI("https://c2id.com/inspect"), endpointMetadata.getIntrospectionEndpointURI());
		assertEquals(new URI("https://c2id.com/revoke"), endpointMetadata.getRevocationEndpointURI());
		assertEquals(new URI("https://c2id.com/par"), endpointMetadata.getPushedAuthorizationRequestEndpointURI());
		assertEquals(new URI("https://c2id.com/jar"), endpointMetadata.getRequestObjectEndpoint());
		assertEquals(new URI("https://c2id.com/device"), endpointMetadata.getDeviceAuthorizationEndpointURI());
		assertEquals(new URI("https://c2id.com/ciba"), endpointMetadata.getBackChannelAuthenticationEndpoint());
		assertEquals(new URI("https://c2id.com/userinfo"), endpointMetadata.getUserInfoEndpointURI());
		assertEquals(new URI("https://c2id.com/session"), endpointMetadata.getCheckSessionIframeURI());
		assertEquals(new URI("https://c2id.com/logout"), endpointMetadata.getEndSessionEndpointURI());
		assertEquals(new URI("https://c2id.com/fed"), endpointMetadata.getFederationRegistrationEndpointURI());
	}
	
	
	public void testCopyConstructor() throws URISyntaxException {
		
		AuthorizationServerEndpointMetadata asEndpointMetadata = new AuthorizationServerEndpointMetadata();
		
		asEndpointMetadata.setAuthorizationEndpointURI(new URI("https://c2id.com/authz"));
		asEndpointMetadata.setTokenEndpointURI(new URI("https://c2id.com/token"));
		asEndpointMetadata.setRegistrationEndpointURI(new URI("https://c2id.com/reg"));
		asEndpointMetadata.setIntrospectionEndpointURI(new URI("https://c2id.com/inspect"));
		asEndpointMetadata.setRevocationEndpointURI(new URI("https://c2id.com/revoke"));
		asEndpointMetadata.setPushedAuthorizationRequestEndpointURI(new URI("https://c2id.com/par"));
		asEndpointMetadata.setRequestObjectEndpoint(new URI("https://c2id.com/jar"));
		asEndpointMetadata.setDeviceAuthorizationEndpointURI(new URI("https://c2id.com/device"));
		asEndpointMetadata.setBackChannelAuthenticationEndpoint(new URI("https://c2id.com/ciba"));
		
		ReadOnlyOIDCProviderEndpointMetadata opEndpointMetadata = new OIDCProviderEndpointMetadata(asEndpointMetadata);
		assertEquals(new URI("https://c2id.com/authz"), opEndpointMetadata.getAuthorizationEndpointURI());
		assertEquals(new URI("https://c2id.com/token"), opEndpointMetadata.getTokenEndpointURI());
		assertEquals(new URI("https://c2id.com/reg"), opEndpointMetadata.getRegistrationEndpointURI());
		assertEquals(new URI("https://c2id.com/inspect"), opEndpointMetadata.getIntrospectionEndpointURI());
		assertEquals(new URI("https://c2id.com/revoke"), opEndpointMetadata.getRevocationEndpointURI());
		assertEquals(new URI("https://c2id.com/par"), opEndpointMetadata.getPushedAuthorizationRequestEndpointURI());
		assertEquals(new URI("https://c2id.com/jar"), opEndpointMetadata.getRequestObjectEndpoint());
		assertEquals(new URI("https://c2id.com/device"), opEndpointMetadata.getDeviceAuthorizationEndpointURI());
		assertEquals(new URI("https://c2id.com/ciba"), opEndpointMetadata.getBackChannelAuthenticationEndpoint());
		assertNull(opEndpointMetadata.getUserInfoEndpointURI());
		assertNull(opEndpointMetadata.getCheckSessionIframeURI());
		assertNull(opEndpointMetadata.getEndSessionEndpointURI());
		assertNull(opEndpointMetadata.getFederationRegistrationEndpointURI());
	}
}