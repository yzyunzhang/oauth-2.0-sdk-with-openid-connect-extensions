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

package com.nimbusds.oauth2.sdk.dpop.verifier;


import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.dpop.DPoPProofFactory;
import com.nimbusds.oauth2.sdk.dpop.DPoPUtils;
import com.nimbusds.oauth2.sdk.id.JWTID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.nimbusds.oauth2.sdk.util.URIUtils;
import com.nimbusds.oauth2.sdk.util.singleuse.SingleUseChecker;


/**
 * DPoP proof JWT verifier.
 */
@ThreadSafe
public class DPoPProofVerifier {
	
	
	/**
	 * The supported JWS algorithms for the DPoP proof JWTs.
	 */
	public static final Set<JWSAlgorithm> SUPPORTED_JWS_ALGORITHMS;
	
	static {
		Set<JWSAlgorithm> supported = new HashSet<>();
		supported.addAll(JWSAlgorithm.Family.EC);
		supported.addAll(JWSAlgorithm.Family.RSA);
		SUPPORTED_JWS_ALGORITHMS = Collections.unmodifiableSet(supported);
	}
	
	private final Set<JWSAlgorithm> acceptedJWSAlgs;
	
	private final String acceptedMethod;
	
	private final URI acceptedURI;
	
	private final long maxAgeSeconds;
	
	private final boolean requireATH;
	
	private final SingleUseChecker<Map.Entry<DPoPIssuer, JWTID>> singleUseChecker;
	
	
	/**
	 * Creates a new DPoP proof JWT verifier.
	 *
	 * @param acceptedMethod   The accepted HTTP request method (case
	 *                         insensitive). Must not be {@code null}.
	 * @param acceptedURI      The accepted endpoint URI. Any query or
	 *                         fragment component will be stripped from it
	 *                         before performing the comparison. Must not
	 *                         be {@code null}.
	 * @param maxAgeSeconds    The maximum acceptable "iat" (issued-at)
	 *                         claim age, in seconds. JWTs older than that
	 *                         will be rejected.
	 * @param requireATH       {@code true} to require an "ath" (access
	 *                         token hash) claim.
	 * @param singleUseChecker The single use checker for the "jti" (JWT
	 *                         ID) claims, {@code null} if not specified.
	 */
	public DPoPProofVerifier(final Set<JWSAlgorithm> acceptedJWSAlgs,
				 final String acceptedMethod,
				 final URI acceptedURI,
				 final long maxAgeSeconds,
				 final boolean requireATH,
				 final SingleUseChecker<Map.Entry<DPoPIssuer, JWTID>> singleUseChecker) {
		
		if (! SUPPORTED_JWS_ALGORITHMS.containsAll(acceptedJWSAlgs)) {
			throw new IllegalArgumentException("Unsupported JWS algorithms: " + acceptedJWSAlgs.retainAll(SUPPORTED_JWS_ALGORITHMS));
		}
		this.acceptedJWSAlgs = acceptedJWSAlgs;
		
		if (StringUtils.isBlank(acceptedMethod)) {
			throw new IllegalArgumentException("The accepted HTTP method must not be null or blank");
		}
		this.acceptedMethod = acceptedMethod;
		
		if (acceptedURI == null) {
			throw new IllegalArgumentException("The accepted URI must not be null");
		}
		this.acceptedURI = URIUtils.getBaseURI(acceptedURI);
		
		this.maxAgeSeconds = maxAgeSeconds;
		
		this.requireATH = requireATH;
		
		this.singleUseChecker = singleUseChecker;
	}
	
	
	/**
	 * Verifies the specified DPoP proof.
	 *
	 * @param proof  The DPoP proof JWT. Must not be {@code null}.
	 * @param issuer Unique identifier for the the DPoP proof issuer, such
	 *               as its client ID. Must not be {@code null}.
	 *
	 * @throws InvalidDPoPProofException If the DPoP proof is invalid.
	 * @throws JOSEException             If an internal JOSE exception is
	 *                                   encountered.
	 */
	public void verify(final SignedJWT proof, final DPoPIssuer issuer)
		throws InvalidDPoPProofException, JOSEException {
		
		try {
			verify(proof, issuer, null);
		} catch (AccessTokenValidationException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
	}
	
	
	/**
	 * Verifies the specified DPoP proof.
	 *
	 * @param proof       The DPoP proof JWT. Must not be {@code null}.
	 * @param issuer      Unique identifier for the the DPoP proof issuer,
	 *                    such as its client ID. Must not be {@code null}.
	 * @param accessToken The received access token, {@code null} if not
	 *                    applicable.
	 *
	 * @throws InvalidDPoPProofException      If the DPoP proof is invalid.
	 * @throws AccessTokenValidationException If an access token is
	 *                                        expected and its validation
	 *                                        failed.
	 * @throws JOSEException                  If an internal JOSE exception
	 *                                        is encountered.
	 */
	public void verify(final SignedJWT proof, final DPoPIssuer issuer, final AccessToken accessToken)
		throws
		InvalidDPoPProofException,
		AccessTokenValidationException,
		JOSEException {
		
		DefaultJWTProcessor<DPoPProofContext> proc = new DefaultJWTProcessor<>();
		
		// Check JWS header "typ"
		proc.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<DPoPProofContext>(DPoPProofFactory.TYPE));
		
		// Use the JWK embedded into the header to validate the JWT signature
		proc.setJWSKeySelector(new DPoPKeySelector(acceptedJWSAlgs));
		
		// Validate the JWT claims
		proc.setJWTClaimsSetVerifier(new DPoPProofClaimsSetVerifier(
			acceptedMethod,
			acceptedURI,
			maxAgeSeconds,
			requireATH,
			singleUseChecker
		));
		
		DPoPProofContext context = new DPoPProofContext(issuer);
		try {
			proc.process(proof, context);
		} catch (BadJOSEException | KeySourceException e) {
			throw new InvalidDPoPProofException("Invalid DPoP proof: " + e.getMessage(), e);
		}
		
		if (context.getAccessTokenHash() != null) {
			
			if (accessToken == null) {
				throw new AccessTokenValidationException("Missing access token");
			}
			
			Base64URL accessTokenHash = DPoPUtils.computeSHA256(accessToken);
			
			if (! context.getAccessTokenHash().equals(accessTokenHash)) {
				throw new AccessTokenValidationException("The access token hash doesn't match the JWT ath claim");
			}
		}
	}
}