package com.nimbusds.oauth2.sdk;


import java.net.MalformedURLException;
import java.net.URL;

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.http.CommonContentTypes;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.oauth2.sdk.util.URLUtils;


/**
 * Tests token error response serialisation and parsing.
 *
 * @author Vladimir Dzhuvinov
 * @version $version$ (2013-01-30)
 */
public class TokenErrorResponseTest extends TestCase {
	
	
	private static URL ERROR_PAGE_URL = null;
	
	
	public void setUp()
		throws MalformedURLException {
		
		ERROR_PAGE_URL = new URL("http://server.example.com/error/123");
	}


	public void testStandardErrors() {
	
		Set<ErrorObject> errors = TokenErrorResponse.getStandardErrors();
	
		assertTrue(errors.contains(OAuth2Error.INVALID_REQUEST));
		assertTrue(errors.contains(OAuth2Error.INVALID_CLIENT));
		assertTrue(errors.contains(OAuth2Error.INVALID_GRANT));
		assertTrue(errors.contains(OAuth2Error.UNAUTHORIZED_CLIENT));
		assertTrue(errors.contains(OAuth2Error.UNSUPPORTED_GRANT_TYPE));
		assertTrue(errors.contains(OAuth2Error.INVALID_SCOPE));
		
		assertEquals(6, errors.size());
	}
	
	
	public void testSerializeAndParse()
		throws Exception {
	
		ErrorObject err = OAuth2Error.INVALID_REQUEST.setURI(ERROR_PAGE_URL);

		TokenErrorResponse r = new TokenErrorResponse(err);
		
		assertEquals(OAuth2Error.INVALID_REQUEST, r.getErrorObject());
		

		HTTPResponse httpResponse = r.toHTTPResponse();
		
		assertEquals(HTTPResponse.SC_BAD_REQUEST, httpResponse.getStatusCode());
		assertEquals(CommonContentTypes.APPLICATION_JSON, httpResponse.getContentType());
		assertEquals("no-store", httpResponse.getCacheControl());
		assertEquals("no-cache", httpResponse.getPragma());
		
		
		JSONObject jsonObject = JSONObjectUtils.parseJSONObject(httpResponse.getContent());	

		assertEquals(OAuth2Error.INVALID_REQUEST.getCode(), (String)jsonObject.get("error"));
		assertEquals(OAuth2Error.INVALID_REQUEST.getDescription(), (String)jsonObject.get("error_description"));
		assertEquals(ERROR_PAGE_URL.toString(), (String)jsonObject.get("error_uri"));
		assertEquals(3, jsonObject.size());
		
		
		r = TokenErrorResponse.parse(httpResponse);
		
		assertEquals(OAuth2Error.INVALID_REQUEST, r.getErrorObject());
	}
}