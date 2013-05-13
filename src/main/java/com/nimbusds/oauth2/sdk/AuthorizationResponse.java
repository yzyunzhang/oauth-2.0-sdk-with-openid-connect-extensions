package com.nimbusds.oauth2.sdk;


import java.net.URL;

import java.util.Map;

import com.nimbusds.oauth2.sdk.id.State;

import com.nimbusds.oauth2.sdk.http.HTTPResponse;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.nimbusds.oauth2.sdk.util.URLUtils;


/**
 * Authorisation endpoint response. This is the base abstract class for
 * authorisation success and error responses.
 *
 * <p>Related specifications:
 *
 * <ul>
 *     <li>OAuth 2.0 (RFC 6749), section 3.1.
 * </ul>
 *
 * @author Vladimir Dzhuvinov
 * @version $version$ (2013-01-28)
 */
public abstract class AuthorizationResponse implements Response {


	/**
	 * The base redirect URI.
	 */
	private final URL redirectURI;


	/**
	 * The optional state parameter to be echoed back to the client.
	 */
	private final State state;


	/**
	 * Creates a new authorisation response.
	 *
	 * @param redirectURI The base redirect URI. Must not be {@code null}.
	 * @param state       The state, {@code null} if not requested.
	 */
	protected AuthorizationResponse(final URL redirectURI, final State state) {

		if (redirectURI == null)
			throw new IllegalArgumentException("The redirect URI must not be null");
		
		this.redirectURI = redirectURI;

		this.state = state;
	}


	/**
	 * Gets the base redirect URI.
	 *
	 * @return The base redirect URI (without the appended error response 
	 *         parameters).
	 */
	public URL getRedirectURI() {
	
		return redirectURI;
	}


	/**
	 * Gets the optional state.
	 *
	 * @return The state, {@code null} if not requested.
	 */
	public State getState() {
	
		return state;
	}


	/**
	 * Returns the parameters of this authorisation response.
	 *
	 * <p>Example parameters (authorisation success):
	 *
	 * <pre>
	 * access_token = 2YotnFZFEjr1zCsicMWpAA
	 * state = xyz
	 * token_type = example
	 * expires_in = 3600
	 * </pre>
	 *
	 * @return The parameters as a map.
	 *
	 * @throws SerializeException If this response couldn't be serialised 
	 *                            to a parameters map.
	 */
	public abstract Map<String,String> toParameters()
		throws SerializeException;


	/**
	 * Returns the URI representation (redirect URI + fragment / query 
	 * string) of this authorisation response.
	 *
	 * <p>Example URI:
	 *
	 * <pre>
	 * http://example.com/cb#access_token=2YotnFZFEjr1zCsicMWpAA
	 * &amp;state=xyz
	 * &amp;token_type=example
	 * &amp;expires_in=3600
	 * </pre>
	 *
	 * @return The URI representation of this authorisation response.
	 *
	 * @throws SerializeException If this response couldn't be serialised 
	 *                            to a URI.
	 */
	public abstract URL toURI()
		throws SerializeException;


	/**
	 * Returns the HTTP response for this authorisation response.
	 *
	 * <p>Example HTTP response (authorisation success):
	 *
	 * <pre>
	 * HTTP/1.1 302 Found
	 * Location: http://example.com/cb#access_token=2YotnFZFEjr1zCsicMWpAA
	 * &amp;state=xyz
	 * &amp;token_type=example
	 * &amp;expires_in=3600
	 * </pre>
	 *
	 * @return The HTTP response matching this authorisation response.
	 *
	 * @throws SerializeException If the response couldn't be serialised to
	 *                            an HTTP response.
	 */
	@Override
	public HTTPResponse toHTTPResponse()
		throws SerializeException {
	
		HTTPResponse response = new HTTPResponse(HTTPResponse.SC_FOUND);
		
		response.setLocation(toURI());
		
		return response;
	}


	/**
	 * Parses an authorisation response.
	 *
	 * @param redirectURI The base redirect URI. Must not be {@code null}.
	 * @param params      The response parameters to parse. Must not be 
	 *                    {@code null}.
	 *
	 * @return The authorisation success or error response.
	 *
	 * @throws ParseException If the parameters couldn't be parsed to an
	 *                        authorisation success or error response.
	 */
	public static AuthorizationResponse parse(final URL redirectURI, final Map<String,String> params)
		throws ParseException {

		if (StringUtils.isDefined(params.get("error")))
			return AuthorizationErrorResponse.parse(redirectURI, params);

		else
			return AuthorizationSuccessResponse.parse(redirectURI, params);
	}


	/**
	 * Parses an authorisation response.
	 *
	 * <p>Use a relative URI if the host, port and path details are not
	 * known:
	 *
	 * <pre>
	 * URL relUrl = new URL("http://?code=Qcb0Orv1...&state=af0ifjsldkj");
	 * AuthorizationResponse = AuthorizationResponse.parse(relURL);
	 * </pre>
	 *
	 * @param uri The URL to parse. May be absolute or relative, with a
	 *            fragment or query string containing the authorisation
	 *            response parameters. Must not be {@code null}.
	 *
	 * @return The authorisation success or error response.
	 *
	 * @throws ParseException If no authorisation response parameters were
	 *                        found in the URL.
	 */
	public static AuthorizationResponse parse(final URL uri)
		throws ParseException {

		Map<String,String> params = null;
		
		if (uri.getRef() != null)
			params = URLUtils.parseParameters(uri.getRef());

		else if (uri.getQuery() != null)
			params = URLUtils.parseParameters(uri.getQuery());

		else
			throw new ParseException("Missing URL fragment or query string");

		
		return parse(URLUtils.getBaseURL(uri), params);
	}


	/**
	 * Parses an authorisation response.
	 *
	 * <p>Example HTTP response (authorisation success):
	 *
	 * <pre>
	 * HTTP/1.1 302 Found
	 * Location: https://client.example.com/cb?code=SplxlOBeZQQYbYS6WxSbIA&amp;state=xyz
	 * </pre>
	 *
	 * @param httpResponse The HTTP response to parse. Must not be 
	 *                     {@code null}.
	 *
	 * @throws ParseException If the HTTP response couldn't be parsed to an 
	 *                        authorisation response.
	 */
	public static AuthorizationResponse parse(final HTTPResponse httpResponse)
		throws ParseException {
		
		if (httpResponse.getStatusCode() != HTTPResponse.SC_FOUND)
			throw new ParseException("Unexpected HTTP status code, must be 302 (Found): " + 
			                         httpResponse.getStatusCode());
		
		URL location = httpResponse.getLocation();
		
		if (location == null)
			throw new ParseException("Missing redirect URL / HTTP Location header");
		
		return parse(location);
	}
}