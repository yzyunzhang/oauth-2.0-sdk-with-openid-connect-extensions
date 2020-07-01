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

package com.nimbusds.openid.connect.sdk.op;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import net.minidev.json.JSONObject;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.langtag.LangTag;
import com.nimbusds.langtag.LangTagException;
import com.nimbusds.oauth2.sdk.GeneralException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerEndpointMetadata;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Identifier;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import com.nimbusds.openid.connect.sdk.Display;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.assurance.IdentityTrustFramework;
import com.nimbusds.openid.connect.sdk.assurance.evidences.IDDocumentType;
import com.nimbusds.openid.connect.sdk.assurance.evidences.IdentityEvidenceType;
import com.nimbusds.openid.connect.sdk.assurance.evidences.IdentityVerificationMethod;
import com.nimbusds.openid.connect.sdk.claims.ACR;
import com.nimbusds.openid.connect.sdk.claims.ClaimType;
import com.nimbusds.openid.connect.sdk.federation.registration.ClientRegistrationType;


/**
 * OpenID Provider (OP) metadata.
 *
 * <p>Related specifications:
 *
 * <ul>
 *     <li>OpenID Connect Discovery 1.0, section 3.
 *     <li>OpenID Connect Session Management 1.0, section 2.1 (draft 28).
 *     <li>OpenID Connect Front-Channel Logout 1.0, section 3 (draft 02).
 *     <li>OpenID Connect Back-Channel Logout 1.0, section 2.1 (draft 04).
 *     <li>OpenID Connect for Identity Assurance 1.0 (draft 08).
 *     <li>OpenID Connect Federation 1.0 (draft 12).
 *     <li>OAuth 2.0 Authorization Server Metadata (RFC 8414)
 *     <li>OAuth 2.0 Mutual TLS Client Authentication and Certificate Bound
 *         Access Tokens (RFC 8705)
 *     <li>Financial-grade API: JWT Secured Authorization Response Mode for
 *         OAuth 2.0 (JARM)
 * </ul>
 */
public class OIDCProviderMetadata extends AuthorizationServerMetadata {


	/**
	 * The registered parameter names.
	 */
	private static final Set<String> REGISTERED_PARAMETER_NAMES;


	static {
		Set<String> p = new HashSet<>(AuthorizationServerMetadata.getRegisteredParameterNames());
		p.addAll(OIDCProviderEndpointMetadata.getRegisteredParameterNames());
		p.add("check_session_iframe");
		p.add("end_session_endpoint");
		p.add("acr_values_supported");
		p.add("subject_types_supported");
		p.add("id_token_signing_alg_values_supported");
		p.add("id_token_encryption_alg_values_supported");
		p.add("id_token_encryption_enc_values_supported");
		p.add("userinfo_signing_alg_values_supported");
		p.add("userinfo_encryption_alg_values_supported");
		p.add("userinfo_encryption_enc_values_supported");
		p.add("display_values_supported");
		p.add("claim_types_supported");
		p.add("claims_supported");
		p.add("claims_locales_supported");
		p.add("claims_parameter_supported");
		p.add("backchannel_logout_supported");
		p.add("backchannel_logout_session_supported");
		p.add("frontchannel_logout_supported");
		p.add("frontchannel_logout_session_supported");
		p.add("verified_claims_supported");
		p.add("trust_frameworks_supported");
		p.add("evidence_supported");
		p.add("id_documents_supported");
		p.add("id_documents_verification_methods_supported");
		p.add("claims_in_verified_claims_supported");
		p.add("client_registration_types_supported");
		p.add("client_registration_authn_methods_supported");
		p.add("organization_name");
		REGISTERED_PARAMETER_NAMES = Collections.unmodifiableSet(p);
	}


	/**
	 * The UserInfo endpoint.
	 */
	private URI userInfoEndpoint;
	
	
	/**
	 * The cross-origin check session iframe.
	 */
	private URI checkSessionIframe;
	
	
	/**
	 * The logout endpoint.
	 */
	private URI endSessionEndpoint;


	/**
	 * The supported ACRs.
	 */
	private List<ACR> acrValues;


	/**
	 * The supported subject types.
	 */
	private final List<SubjectType> subjectTypes;


	/**
	 * The supported ID token JWS algorithms.
	 */
	private List<JWSAlgorithm> idTokenJWSAlgs;


	/**
	 * The supported ID token JWE algorithms.
	 */
	private List<JWEAlgorithm> idTokenJWEAlgs;


	/**
	 * The supported ID token encryption methods.
	 */
	private List<EncryptionMethod> idTokenJWEEncs;


	/**
	 * The supported UserInfo JWS algorithms.
	 */
	private List<JWSAlgorithm> userInfoJWSAlgs;


	/**
	 * The supported UserInfo JWE algorithms.
	 */
	private List<JWEAlgorithm> userInfoJWEAlgs;


	/**
	 * The supported UserInfo encryption methods.
	 */
	private List<EncryptionMethod> userInfoJWEEncs;


	/**
	 * The supported displays.
	 */
	private List<Display> displays;
	
	
	/**
	 * The supported claim types.
	 */
	private List<ClaimType> claimTypes;


	/**
	 * The supported claims names.
	 */
	private List<String> claims;
	
	
	/**
	 * The supported claims locales.
	 */
	private List<LangTag> claimsLocales;
	
	
	/**
	 * If {@code true} the {@code claims} parameter is supported, else not.
	 */
	private boolean claimsParamSupported = false;
	
	
	/**
	 * If {@code true} the {@code frontchannel_logout_supported} parameter
	 * is set, else not.
	 */
	private boolean frontChannelLogoutSupported = false;
	
	
	/**
	 * If {@code true} the {@code frontchannel_logout_session_supported}
	 * parameter is set, else not.
	 */
	private boolean frontChannelLogoutSessionSupported = false;
	
	
	/**
	 * If {@code true} the {@code backchannel_logout_supported} parameter
	 * is set, else not.
	 */
	private boolean backChannelLogoutSupported = false;
	
	
	/**
	 * If {@code true} the {@code backchannel_logout_session_supported}
	 * parameter is set, else not.
	 */
	private boolean backChannelLogoutSessionSupported = false;
	
	
	/**
	 * If {@code true} verified claims are supported.
	 */
	private boolean verifiedClaimsSupported = false;
	
	
	/**
	 * The supported trust frameworks.
	 */
	private List<IdentityTrustFramework> trustFrameworks;
	
	
	/**
	 * The supported identity evidence types.
	 */
	private List<IdentityEvidenceType> evidenceTypes;
	
	
	/**
	 * The supported identity documents.
	 */
	private List<IDDocumentType> idDocuments;
	
	
	/**
	 * The supported identity verification methods.
	 */
	private List<IdentityVerificationMethod> idVerificationMethods;
	
	
	/**
	 * The supported verified claims.
	 */
	private List<String> verifiedClaims;
	
	
	/**
	 * The supported federation client registration types.
	 */
	private List<ClientRegistrationType> clientRegistrationTypes;
	
	
	/**
	 * The supported client authentication methods for automatic federation
	 * client registration.
	 */
	private Map<String,List<ClientAuthenticationMethod>> clientRegistrationAuthMethods;
	
	
	/**
	 * The organisation name (in federation).
	 */
	private String organizationName;
	
	
	/**
	 * The federation registration endpoint.
	 */
	private URI federationRegistrationEndpoint;


	/**
	 * Creates a new OpenID Connect provider metadata instance.
	 * 
	 * @param issuer       The issuer identifier. Must be an URI using the
	 *                     https scheme with no query or fragment 
	 *                     component. Must not be {@code null}.
	 * @param subjectTypes The supported subject types. At least one must
	 *                     be specified. Must not be {@code null}.
	 * @param jwkSetURI    The JWK set URI. Must not be {@code null}.
	 */
	public OIDCProviderMetadata(final Issuer issuer,
				    final List<SubjectType> subjectTypes,
				    final URI jwkSetURI) {
	
		super(issuer);
		
		if (subjectTypes.size() < 1)
			throw new IllegalArgumentException("At least one supported subject type must be specified");
		
		this.subjectTypes = subjectTypes;

		if (jwkSetURI == null)
			throw new IllegalArgumentException("The public JWK set URI must not be null");

		setJWKSetURI(jwkSetURI);
		
		// Default OpenID Connect setting is supported
		setSupportsRequestURIParam(true);
	}
	
	
	@Override
	public void setMtlsEndpointAliases(AuthorizationServerEndpointMetadata mtlsEndpointAliases) {
	
		if (mtlsEndpointAliases != null && !(mtlsEndpointAliases instanceof OIDCProviderEndpointMetadata)) {
			// convert the provided endpoints to OIDC
			super.setMtlsEndpointAliases(new OIDCProviderEndpointMetadata(mtlsEndpointAliases));
		} else {
			super.setMtlsEndpointAliases(mtlsEndpointAliases);
		}
	}
	
	@Override
	public OIDCProviderEndpointMetadata getMtlsEndpointAliases() {
	
		return (OIDCProviderEndpointMetadata) super.getMtlsEndpointAliases();
	}


	/**
	 * Gets the registered OpenID Connect provider metadata parameter
	 * names.
	 *
	 * @return The registered OpenID Connect provider metadata parameter
	 *         names, as an unmodifiable set.
	 */
	public static Set<String> getRegisteredParameterNames() {

		return REGISTERED_PARAMETER_NAMES;
	}


	/**
	 * Gets the UserInfo endpoint URI. Corresponds the
	 * {@code userinfo_endpoint} metadata field.
	 *
	 * @return The UserInfo endpoint URI, {@code null} if not specified.
	 */
	public URI getUserInfoEndpointURI() {

		return userInfoEndpoint;
	}


	/**
	 * Sets the UserInfo endpoint URI. Corresponds the
	 * {@code userinfo_endpoint} metadata field.
	 *
	 * @param userInfoEndpoint The UserInfo endpoint URI, {@code null} if
	 *                         not specified.
	 */
	public void setUserInfoEndpointURI(final URI userInfoEndpoint) {

		this.userInfoEndpoint = userInfoEndpoint;
	}
	
	
	/**
	 * Gets the cross-origin check session iframe URI. Corresponds to the
	 * {@code check_session_iframe} metadata field.
	 * 
	 * @return The check session iframe URI, {@code null} if not specified.
	 */
	public URI getCheckSessionIframeURI() {
		
		return checkSessionIframe;
	}


	/**
	 * Sets the cross-origin check session iframe URI. Corresponds to the
	 * {@code check_session_iframe} metadata field.
	 *
	 * @param checkSessionIframe The check session iframe URI, {@code null}
	 *                           if not specified.
	 */
	public void setCheckSessionIframeURI(final URI checkSessionIframe) {

		this.checkSessionIframe = checkSessionIframe;
	}
	
	
	/**
	 * Gets the logout endpoint URI. Corresponds to the
	 * {@code end_session_endpoint} metadata field.
	 * 
	 * @return The logoout endpoint URI, {@code null} if not specified.
	 */
	public URI getEndSessionEndpointURI() {
		
		return endSessionEndpoint;
	}


	/**
	 * Sets the logout endpoint URI. Corresponds to the
	 * {@code end_session_endpoint} metadata field.
	 *
	 * @param endSessionEndpoint The logoout endpoint URI, {@code null} if
	 *                           not specified.
	 */
	public void setEndSessionEndpointURI(final URI endSessionEndpoint) {

		this.endSessionEndpoint = endSessionEndpoint;
	}

	/**
	 * Gets the supported Authentication Context Class References (ACRs).
	 * Corresponds to the {@code acr_values_supported} metadata field.
	 *
	 * @return The supported ACRs, {@code null} if not specified.
	 */
	public List<ACR> getACRs() {

		return acrValues;
	}


	/**
	 * Sets the supported Authentication Context Class References (ACRs).
	 * Corresponds to the {@code acr_values_supported} metadata field.
	 *
	 * @param acrValues The supported ACRs, {@code null} if not specified.
	 */
	public void setACRs(final List<ACR> acrValues) {

		this.acrValues = acrValues;
	}


	/**
	 * Gets the supported subject types. Corresponds to the
	 * {@code subject_types_supported} metadata field.
	 *
	 * @return The supported subject types.
	 */
	public List<SubjectType> getSubjectTypes() {

		return subjectTypes;
	}


	/**
	 * Gets the supported JWS algorithms for ID tokens. Corresponds to the 
	 * {@code id_token_signing_alg_values_supported} metadata field.
	 *
	 * @return The supported JWS algorithms, {@code null} if not specified.
	 */
	public List<JWSAlgorithm> getIDTokenJWSAlgs() {

		return idTokenJWSAlgs;
	}


	/**
	 * Sets the supported JWS algorithms for ID tokens. Corresponds to the
	 * {@code id_token_signing_alg_values_supported} metadata field.
	 *
	 * @param idTokenJWSAlgs The supported JWS algorithms, {@code null} if
	 *                       not specified.
	 */
	public void setIDTokenJWSAlgs(final List<JWSAlgorithm> idTokenJWSAlgs) {

		this.idTokenJWSAlgs = idTokenJWSAlgs;
	}


	/**
	 * Gets the supported JWE algorithms for ID tokens. Corresponds to the 
	 * {@code id_token_encryption_alg_values_supported} metadata field.
	 *
	 * @return The supported JWE algorithms, {@code null} if not specified.
	 */
	public List<JWEAlgorithm> getIDTokenJWEAlgs() {

		return idTokenJWEAlgs;
	}


	/**
	 * Sets the supported JWE algorithms for ID tokens. Corresponds to the
	 * {@code id_token_encryption_alg_values_supported} metadata field.
	 *
	 * @param idTokenJWEAlgs The supported JWE algorithms, {@code null} if
	 *                       not specified.
	 */
	public void setIDTokenJWEAlgs(final List<JWEAlgorithm> idTokenJWEAlgs) {

		this.idTokenJWEAlgs = idTokenJWEAlgs;
	}


	/**
	 * Gets the supported encryption methods for ID tokens. Corresponds to 
	 * the {@code id_token_encryption_enc_values_supported} metadata field.
	 *
	 * @return The supported encryption methods, {@code null} if not 
	 *         specified.
	 */
	public List<EncryptionMethod> getIDTokenJWEEncs() {

		return idTokenJWEEncs;
	}


	/**
	 * Sets the supported encryption methods for ID tokens. Corresponds to
	 * the {@code id_token_encryption_enc_values_supported} metadata field.
	 *
	 * @param idTokenJWEEncs The supported encryption methods, {@code null}
	 *                       if not specified.
	 */
	public void setIDTokenJWEEncs(final List<EncryptionMethod> idTokenJWEEncs) {

		this.idTokenJWEEncs = idTokenJWEEncs;
	}


	/**
	 * Gets the supported JWS algorithms for UserInfo JWTs. Corresponds to 
	 * the {@code userinfo_signing_alg_values_supported} metadata field.
	 *
	 * @return The supported JWS algorithms, {@code null} if not specified.
	 */
	public List<JWSAlgorithm> getUserInfoJWSAlgs() {

		return userInfoJWSAlgs;
	}


	/**
	 * Sets the supported JWS algorithms for UserInfo JWTs. Corresponds to
	 * the {@code userinfo_signing_alg_values_supported} metadata field.
	 *
	 * @param userInfoJWSAlgs The supported JWS algorithms, {@code null} if
	 *                        not specified.
	 */
	public void setUserInfoJWSAlgs(final List<JWSAlgorithm> userInfoJWSAlgs) {

		this.userInfoJWSAlgs = userInfoJWSAlgs;
	}


	/**
	 * Gets the supported JWE algorithms for UserInfo JWTs. Corresponds to 
	 * the {@code userinfo_encryption_alg_values_supported} metadata field.
	 *
	 * @return The supported JWE algorithms, {@code null} if not specified.
	 */
	public List<JWEAlgorithm> getUserInfoJWEAlgs() {

		return userInfoJWEAlgs;
	}


	/**
	 * Sets the supported JWE algorithms for UserInfo JWTs. Corresponds to
	 * the {@code userinfo_encryption_alg_values_supported} metadata field.
	 *
	 * @param userInfoJWEAlgs The supported JWE algorithms, {@code null} if
	 *                        not specified.
	 */
	public void setUserInfoJWEAlgs(final List<JWEAlgorithm> userInfoJWEAlgs) {

		this.userInfoJWEAlgs = userInfoJWEAlgs;
	}


	/**
	 * Gets the supported encryption methods for UserInfo JWTs. Corresponds 
	 * to the {@code userinfo_encryption_enc_values_supported} metadata 
	 * field.
	 *
	 * @return The supported encryption methods, {@code null} if not 
	 *         specified.
	 */
	public List<EncryptionMethod> getUserInfoJWEEncs() {

		return userInfoJWEEncs;
	}


	/**
	 * Sets the supported encryption methods for UserInfo JWTs. Corresponds
	 * to the {@code userinfo_encryption_enc_values_supported} metadata
	 * field.
	 *
	 * @param userInfoJWEEncs The supported encryption methods,
	 *                        {@code null} if not specified.
	 */
	public void setUserInfoJWEEncs(final List<EncryptionMethod> userInfoJWEEncs) {

		this.userInfoJWEEncs = userInfoJWEEncs;
	}


	/**
	 * Gets the supported displays. Corresponds to the 
	 * {@code display_values_supported} metadata field.
	 *
	 * @return The supported displays, {@code null} if not specified.
	 */
	public List<Display> getDisplays() {

		return displays;
	}


	/**
	 * Sets the supported displays. Corresponds to the
	 * {@code display_values_supported} metadata field.
	 *
	 * @param displays The supported displays, {@code null} if not
	 *                 specified.
	 */
	public void setDisplays(final List<Display> displays) {

		this.displays = displays;
	}
	
	
	/**
	 * Gets the supported claim types. Corresponds to the 
	 * {@code claim_types_supported} metadata field.
	 * 
	 * @return The supported claim types, {@code null} if not specified.
	 */
	public List<ClaimType> getClaimTypes() {
		
		return claimTypes;
	}


	/**
	 * Sets the supported claim types. Corresponds to the
	 * {@code claim_types_supported} metadata field.
	 *
	 * @param claimTypes The supported claim types, {@code null} if not
	 *                   specified.
	 */
	public void setClaimTypes(final List<ClaimType> claimTypes) {

		this.claimTypes = claimTypes;
	}


	/**
	 * Gets the supported claims names. Corresponds to the 
	 * {@code claims_supported} metadata field.
	 *
	 * @return The supported claims names, {@code null} if not specified.
	 */
	public List<String> getClaims() {

		return claims;
	}


	/**
	 * Sets the supported claims names. Corresponds to the
	 * {@code claims_supported} metadata field.
	 *
	 * @param claims The supported claims names, {@code null} if not
	 *               specified.
	 */
	public void setClaims(final List<String> claims) {

		this.claims = claims;
	}
	
	
	/**
	 * Gets the supported claims locales. Corresponds to the
	 * {@code claims_locales_supported} metadata field.
	 * 
	 * @return The supported claims locales, {@code null} if not specified.
	 */
	public List<LangTag> getClaimsLocales() {
		
		return claimsLocales;
	}


	/**
	 * Sets the supported claims locales. Corresponds to the
	 * {@code claims_locales_supported} metadata field.
	 *
	 * @param claimsLocales The supported claims locales, {@code null} if
	 *                      not specified.
	 */
	public void setClaimLocales(final List<LangTag> claimsLocales) {

		this.claimsLocales = claimsLocales;
	}
	
	
	/**
	 * Gets the support for the {@code claims} authorisation request
	 * parameter. Corresponds to the {@code claims_parameter_supported} 
	 * metadata field.
	 * 
	 * @return {@code true} if the {@code claim} parameter is supported,
	 *         else {@code false}.
	 */
	public boolean supportsClaimsParam() {
		
		return claimsParamSupported;
	}


	/**
	 * Sets the support for the {@code claims} authorisation request
	 * parameter. Corresponds to the {@code claims_parameter_supported}
	 * metadata field.
	 *
	 * @param claimsParamSupported {@code true} if the {@code claim}
	 *                             parameter is supported, else
	 *                             {@code false}.
	 */
	public void setSupportsClaimsParams(final boolean claimsParamSupported) {

		this.claimsParamSupported = claimsParamSupported;
	}
	
	
	/**
	 * Gets the support for front-channel logout. Corresponds to the
	 * {@code frontchannel_logout_supported} metadata field.
	 *
	 * @return {@code true} if front-channel logout is supported, else
	 *         {@code false}.
	 */
	public boolean supportsFrontChannelLogout() {
		
		return frontChannelLogoutSupported;
	}
	
	
	/**
	 * Sets the support for front-channel logout. Corresponds to the
	 * {@code frontchannel_logout_supported} metadata field.
	 *
	 * @param frontChannelLogoutSupported {@code true} if front-channel
	 *                                    logout is supported, else
	 *                                    {@code false}.
	 */
	public void setSupportsFrontChannelLogout(final boolean frontChannelLogoutSupported) {
	
		this.frontChannelLogoutSupported = frontChannelLogoutSupported;
	}
	
	
	/**
	 * Gets the support for front-channel logout with a session ID.
	 * Corresponds to the {@code frontchannel_logout_session_supported}
	 * metadata field.
	 *
	 * @return {@code true} if front-channel logout with a session ID is
	 *         supported, else {@code false}.
	 */
	public boolean supportsFrontChannelLogoutSession() {
		
		return frontChannelLogoutSessionSupported;
	}
	
	
	/**
	 * Sets the support for front-channel logout with a session ID.
	 * Corresponds to the {@code frontchannel_logout_session_supported}
	 * metadata field.
	 *
	 * @param frontChannelLogoutSessionSupported {@code true} if
	 *                                           front-channel logout with
	 *                                           a session ID is supported,
	 *                                           else {@code false}.
	 */
	public void setSupportsFrontChannelLogoutSession(final boolean frontChannelLogoutSessionSupported) {
	
		this.frontChannelLogoutSessionSupported = frontChannelLogoutSessionSupported;
	}
	
	
	/**
	 * Gets the support for back-channel logout. Corresponds to the
	 * {@code backchannel_logout_supported} metadata field.
	 *
	 * @return {@code true} if back-channel logout is supported, else
	 *         {@code false}.
	 */
	public boolean supportsBackChannelLogout() {
		
		return backChannelLogoutSupported;
	}
	
	
	/**
	 * Sets the support for back-channel logout. Corresponds to the
	 * {@code backchannel_logout_supported} metadata field.
	 *
	 * @param backChannelLogoutSupported {@code true} if back-channel
	 *                                   logout is supported, else
	 *                                   {@code false}.
	 */
	public void setSupportsBackChannelLogout(final boolean backChannelLogoutSupported) {
	
		this.backChannelLogoutSupported = backChannelLogoutSupported;
	}
	
	
	/**
	 * Gets the support for back-channel logout with a session ID.
	 * Corresponds to the {@code backchannel_logout_session_supported}
	 * metadata field.
	 *
	 * @return {@code true} if back-channel logout with a session ID is
	 *         supported, else {@code false}.
	 */
	public boolean supportsBackChannelLogoutSession() {
		
		return backChannelLogoutSessionSupported;
	}
	
	
	/**
	 * Sets the support for back-channel logout with a session ID.
	 * Corresponds to the {@code backchannel_logout_session_supported}
	 * metadata field.
	 *
	 * @param backChannelLogoutSessionSupported {@code true} if
	 *                                          back-channel logout with a
	 *                                          session ID is supported,
	 *                                          else {@code false}.
	 */
	public void setSupportsBackChannelLogoutSession(final boolean backChannelLogoutSessionSupported) {
		
		this.backChannelLogoutSessionSupported = backChannelLogoutSessionSupported;
	}
	
	
	/**
	 * Gets support for verified claims. Corresponds to the
	 * {@code verified_claims_supported} metadata field.
	 *
	 * @return {@code true} if verified claims are supported, else
	 *         {@code false}.
	 */
	public boolean supportsVerifiedClaims() {
		
		return verifiedClaimsSupported;
	}
	
	
	/**
	 * Sets support for verified claims. Corresponds to the
	 * {@code verified_claims_supported} metadata field.
	 *
	 * @param verifiedClaimsSupported {@code true} if verified claims are
	 *                                supported, else {@code false}.
	 */
	public void setSupportsVerifiedClaims(final boolean verifiedClaimsSupported) {
		
		this.verifiedClaimsSupported = verifiedClaimsSupported;
	}
	
	
	/**
	 * Gets the supported identity trust frameworks. Corresponds to the
	 * {@code trust_frameworks_supported} metadata field.
	 *
	 * @return The supported identity trust frameworks, {@code null} if not
	 *         specified.
	 */
	public List<IdentityTrustFramework> getIdentityTrustFrameworks() {
		return trustFrameworks;
	}
	
	
	/**
	 * Sets the supported identity trust frameworks. Corresponds to the
	 * {@code trust_frameworks_supported} metadata field.
	 *
	 * @param trustFrameworks The supported identity trust frameworks,
	 *                        {@code null} if not specified.
	 */
	public void setIdentityTrustFrameworks(final List<IdentityTrustFramework> trustFrameworks) {
		this.trustFrameworks = trustFrameworks;
	}
	
	
	/**
	 * Gets the supported identity evidence types. Corresponds to the
	 * {@code evidence_supported} metadata field.
	 *
	 * @return The supported identity evidence types, {@code null} if not
	 *         specified.
	 */
	public List<IdentityEvidenceType> getIdentityEvidenceTypes() {
		return evidenceTypes;
	}
	
	
	/**
	 * Sets the supported identity evidence types. Corresponds to the
	 * {@code evidence_supported} metadata field.
	 *
	 * @param evidenceTypes The supported identity evidence types,
	 *                      {@code null} if not specified.
	 */
	public void setIdentityEvidenceTypes(final List<IdentityEvidenceType> evidenceTypes) {
		this.evidenceTypes = evidenceTypes;
	}
	
	
	/**
	 * Gets the supported identity document types. Corresponds to the
	 * {@code id_documents_supported} metadata field.
	 *
	 * @return The supported identity documents types, {@code null}
	 *         if not specified.
	 */
	public List<IDDocumentType> getIdentityDocumentTypes() {
		return idDocuments;
	}
	
	
	/**
	 * Sets the supported identity document types. Corresponds to the
	 * {@code id_documents_supported} metadata field.
	 *
	 * @param idDocuments The supported identity document types,
	 *                    {@code null} if not specified.
	 */
	public void setIdentityDocumentTypes(List<IDDocumentType> idDocuments) {
		this.idDocuments = idDocuments;
	}
	
	
	/**
	 * Gets the supported identity verification methods. Corresponds to the
	 * {@code id_documents_verification_methods_supported} metadata field.
	 *
	 * @return The supported identity verification methods, {@code null}
	 *         if not specified.
	 */
	public List<IdentityVerificationMethod> getIdentityVerificationMethods() {
		return idVerificationMethods;
	}
	
	
	/**
	 * Sets the supported identity verification methods. Corresponds to the
	 * {@code id_documents_verification_methods_supported} metadata field.
	 *
	 * @param idVerificationMethods The supported identity verification
	 *                              methods, {@code null} if not specified.
	 */
	public void setIdentityVerificationMethods(final List<IdentityVerificationMethod> idVerificationMethods) {
		this.idVerificationMethods = idVerificationMethods;
	}
	
	
	/**
	 * Gets the supported verified claims names. Corresponds to the
	 * {@code claims_in_verified_claims_supported} metadata field.
	 *
	 * @return The supported verified claims names, {@code null} if not
	 *         specified.
	 */
	public List<String> getVerifiedClaims() {
		return verifiedClaims;
	}
	
	
	/**
	 * Sets the supported verified claims names. Corresponds to the
	 * {@code claims_in_verified_claims_supported} metadata field.
	 *
	 * @param verifiedClaims The supported verified claims names,
	 *                       {@code null} if not specified.
	 */
	public void setVerifiedClaims(final List<String> verifiedClaims) {
		this.verifiedClaims = verifiedClaims;
	}
	
	
	/**
	 * Gets the supported federation client registration types. Corresponds
	 * to the {@code client_registration_types_supported} metadata field.
	 *
	 * @return The supported client registration types, {@code null} if not
	 *         specified.
	 */
	public List<ClientRegistrationType> getClientRegistrationTypes() {
		return clientRegistrationTypes;
	}
	
	
	/**
	 * Sets the supported federation client registration types. Corresponds
	 * to the {@code client_registration_types_supported} metadata field.
	 *
	 * @param clientRegistrationTypes The supported client registration
	 *                                types, {@code null} if not specified.
	 */
	public void setClientRegistrationTypes(final List<ClientRegistrationType> clientRegistrationTypes) {
		this.clientRegistrationTypes = clientRegistrationTypes;
	}
	
	
	/**
	 * Gets the supported client authentication methods for automatic
	 * federation client registration. Corresponds to the
	 * {@code client_registration_authn_methods_supported} field.
	 *
	 * @return The supported authentication methods for automatic
	 *         federation client registration, {@code null} if not
	 *         specified.
	 */
	public Map<String,List<ClientAuthenticationMethod>> getClientRegistrationAuthnMethods() {
		return clientRegistrationAuthMethods;
	}
	
	
	/**
	 * Sets the supported client authentication methods for automatic
	 * federation client registration. Corresponds to the
	 * {@code client_registration_authn_methods_supported} field.
	 *
	 * @param methods The supported authentication methods for automatic
	 *                federation client registration, {@code null} if not
	 *                specified.
	 */
	public void setClientRegistrationAuthnMethods(final Map<String,List<ClientAuthenticationMethod>> methods) {
		clientRegistrationAuthMethods = methods;
	}
	
	
	/**
	 * Gets the organisation name (in federation). Corresponds to the
	 * {@code organization_name} metadata field.
	 *
	 * @return The organisation name, {@code null} if not specified.
	 */
	public String getOrganizationName() {
		return organizationName;
	}
	
	
	/**
	 * Sets the organisation name (in federation). Corresponds to the
	 * {@code organization_name} metadata field.
	 *
	 * @param organizationName The organisation name, {@code null} if not
	 *                         specified.
	 */
	public void setOrganizationName(final String organizationName) {
		this.organizationName = organizationName;
	}
	
	
	/**
	 * Gets the federation registration endpoint URI. Corresponds to the
	 * {@code federation_registration_endpoint} metadata field.
	 *
	 * @return The federation registration endpoint URI, {@code null} if
	 *         not specified.
	 */
	public URI getFederationRegistrationEndpointURI() {
		
		return federationRegistrationEndpoint;
	}
	
	
	/**
	 * Sets the federation registration endpoint URI. Corresponds to the
	 * {@code federation_registration_endpoint} metadata field.
	 *
	 * @param federationRegistrationEndpoint The federation registration
	 *                                       endpoint URI, {@code null} if
	 *                                       not specified.
	 */
	public void setFederationRegistrationEndpointURI(final URI federationRegistrationEndpoint) {
		
		this.federationRegistrationEndpoint = federationRegistrationEndpoint;
	}
	
	
	/**
	 * Applies the OpenID Provider metadata defaults where no values have
	 * been specified.
	 *
	 * <ul>
	 *     <li>The response modes default to {@code ["query", "fragment"]}.
	 *     <li>The grant types default to {@code ["authorization_code",
	 *         "implicit"]}.
	 *     <li>The token endpoint authentication methods default to
	 *         {@code ["client_secret_basic"]}.
	 *     <li>The claim types default to {@code ["normal]}.
	 * </ul>
	 */
	public void applyDefaults() {

		super.applyDefaults();

		if (claimTypes == null) {
			claimTypes = new ArrayList<>(1);
			claimTypes.add(ClaimType.NORMAL);
		}
	}


	/**
	 * Returns the JSON object representation of this OpenID Connect
	 * provider metadata.
	 *
	 * @return The JSON object representation.
	 */
	public JSONObject toJSONObject() {

		JSONObject o = super.toJSONObject();

		// Mandatory fields

		List<String> stringList = new ArrayList<>(subjectTypes.size());

		for (SubjectType st: subjectTypes)
			stringList.add(st.toString());

		o.put("subject_types_supported", stringList);

		// Optional fields

		if (userInfoEndpoint != null)
			o.put("userinfo_endpoint", userInfoEndpoint.toString());

		if (checkSessionIframe != null)
			o.put("check_session_iframe", checkSessionIframe.toString());

		if (endSessionEndpoint != null)
			o.put("end_session_endpoint", endSessionEndpoint.toString());

		if (acrValues != null) {
			o.put("acr_values_supported", Identifier.toStringList(acrValues));
		}

		if (idTokenJWSAlgs != null) {

			stringList = new ArrayList<>(idTokenJWSAlgs.size());

			for (JWSAlgorithm alg: idTokenJWSAlgs)
				stringList.add(alg.getName());

			o.put("id_token_signing_alg_values_supported", stringList);
		}

		if (idTokenJWEAlgs != null) {

			stringList = new ArrayList<>(idTokenJWEAlgs.size());

			for (JWEAlgorithm alg: idTokenJWEAlgs)
				stringList.add(alg.getName());

			o.put("id_token_encryption_alg_values_supported", stringList);
		}

		if (idTokenJWEEncs != null) {

			stringList = new ArrayList<>(idTokenJWEEncs.size());

			for (EncryptionMethod m: idTokenJWEEncs)
				stringList.add(m.getName());

			o.put("id_token_encryption_enc_values_supported", stringList);
		}

		if (userInfoJWSAlgs != null) {

			stringList = new ArrayList<>(userInfoJWSAlgs.size());

			for (JWSAlgorithm alg: userInfoJWSAlgs)
				stringList.add(alg.getName());

			o.put("userinfo_signing_alg_values_supported", stringList);
		}

		if (userInfoJWEAlgs != null) {

			stringList = new ArrayList<>(userInfoJWEAlgs.size());

			for (JWEAlgorithm alg: userInfoJWEAlgs)
				stringList.add(alg.getName());

			o.put("userinfo_encryption_alg_values_supported", stringList);
		}

		if (userInfoJWEEncs != null) {

			stringList = new ArrayList<>(userInfoJWEEncs.size());

			for (EncryptionMethod m: userInfoJWEEncs)
				stringList.add(m.getName());

			o.put("userinfo_encryption_enc_values_supported", stringList);
		}

		if (displays != null) {

			stringList = new ArrayList<>(displays.size());

			for (Display d: displays)
				stringList.add(d.toString());

			o.put("display_values_supported", stringList);
		}

		if (claimTypes != null) {

			stringList = new ArrayList<>(claimTypes.size());

			for (ClaimType ct: claimTypes)
				stringList.add(ct.toString());

			o.put("claim_types_supported", stringList);
		}

		if (claims != null)
			o.put("claims_supported", claims);

		if (claimsLocales != null) {

			stringList = new ArrayList<>(claimsLocales.size());

			for (LangTag l: claimsLocales)
				stringList.add(l.toString());

			o.put("claims_locales_supported", stringList);
		}

		if (claimsParamSupported) {
			o.put("claims_parameter_supported", true);
		}
		
		if (supportsRequestURIParam()) {
			// default true
			o.remove("request_uri_parameter_supported");
		} else {
			o.put("request_uri_parameter_supported", false);
		}
		
		// optional front and back-channel logout
		if (frontChannelLogoutSupported) {
			o.put("frontchannel_logout_supported", true);
		}
		
		if (frontChannelLogoutSupported) {
			o.put("frontchannel_logout_session_supported", frontChannelLogoutSessionSupported);
		}
		
		if (backChannelLogoutSupported) {
			o.put("backchannel_logout_supported", true);
		}
		
		if (backChannelLogoutSupported) {
			o.put("backchannel_logout_session_supported", backChannelLogoutSessionSupported);
		}
		
		// identity assurance
		
		if (verifiedClaimsSupported) {
			o.put("verified_claims_supported", true);
			if (trustFrameworks != null) {
				o.put("trust_frameworks_supported", Identifier.toStringList(trustFrameworks));
			}
			if (evidenceTypes != null) {
				o.put("evidence_supported", Identifier.toStringList(evidenceTypes));
			}
			if (idDocuments != null) {
				o.put("id_documents_supported", Identifier.toStringList(idDocuments));
			}
			if (idVerificationMethods != null) {
				o.put("id_documents_verification_methods_supported", Identifier.toStringList(idVerificationMethods));
			}
			if (verifiedClaims != null) {
				o.put("claims_in_verified_claims_supported", verifiedClaims);
			}
		}
		
		// federation
		
		if (CollectionUtils.isNotEmpty(clientRegistrationTypes)) {
			o.put("client_registration_types_supported", Identifier.toStringList(clientRegistrationTypes));
		}
		if (MapUtils.isNotEmpty(clientRegistrationAuthMethods)) {
			JSONObject map = new JSONObject();
			for (Map.Entry<String,List<ClientAuthenticationMethod>> en: getClientRegistrationAuthnMethods().entrySet()) {
				List<String> methodNames = new LinkedList<>();
				for (ClientAuthenticationMethod method: en.getValue()) {
					methodNames.add(method.getValue());
				}
				map.put(en.getKey(), methodNames);
			}
			o.put("client_registration_authn_methods_supported", map);
		}
		if (organizationName != null) {
			o.put("organization_name", organizationName);
		}
		if (federationRegistrationEndpoint != null) {
			o.put("federation_registration_endpoint", federationRegistrationEndpoint.toString());
		}
		
		return o;
	}
	
	
	/**
	 * Parses an OpenID Provider metadata from the specified JSON object.
	 *
	 * @param jsonObject The JSON object to parse. Must not be 
	 *                   {@code null}.
	 *
	 * @return The OpenID Provider metadata.
	 *
	 * @throws ParseException If the JSON object couldn't be parsed to an
	 *                        OpenID Provider metadata.
	 */
	public static OIDCProviderMetadata parse(final JSONObject jsonObject)
		throws ParseException {
		
		AuthorizationServerMetadata as = AuthorizationServerMetadata.parse(jsonObject);

		List<SubjectType> subjectTypes = new ArrayList<>();
		for (String v: JSONObjectUtils.getStringArray(jsonObject, "subject_types_supported")) {
			subjectTypes.add(SubjectType.parse(v));
		}
		
		OIDCProviderMetadata op = new OIDCProviderMetadata(
			as.getIssuer(),
			Collections.unmodifiableList(subjectTypes),
			as.getJWKSetURI());

		// Endpoints
		op.setAuthorizationEndpointURI(as.getAuthorizationEndpointURI());
		op.setTokenEndpointURI(as.getTokenEndpointURI());
		op.setRegistrationEndpointURI(as.getRegistrationEndpointURI());
		op.setIntrospectionEndpointURI(as.getIntrospectionEndpointURI());
		op.setRevocationEndpointURI(as.getRevocationEndpointURI());
		op.setRequestObjectEndpoint(as.getRequestObjectEndpoint());
		op.setPushedAuthorizationRequestEndpointURI(as.getPushedAuthorizationRequestEndpointURI());
		op.userInfoEndpoint = JSONObjectUtils.getURI(jsonObject, "userinfo_endpoint", null);
		op.checkSessionIframe = JSONObjectUtils.getURI(jsonObject, "check_session_iframe", null);
		op.endSessionEndpoint = JSONObjectUtils.getURI(jsonObject, "end_session_endpoint", null);

		// Capabilities
		op.setScopes(as.getScopes());
		op.setResponseTypes(as.getResponseTypes());
		op.setResponseModes(as.getResponseModes());
		op.setGrantTypes(as.getGrantTypes());
		
		op.setTokenEndpointAuthMethods(as.getTokenEndpointAuthMethods());
		op.setTokenEndpointJWSAlgs(as.getTokenEndpointJWSAlgs());
		
		op.setIntrospectionEndpointAuthMethods(as.getIntrospectionEndpointAuthMethods());
		op.setIntrospectionEndpointJWSAlgs(as.getIntrospectionEndpointJWSAlgs());
		
		op.setRevocationEndpointAuthMethods(as.getRevocationEndpointAuthMethods());
		op.setRevocationEndpointJWSAlgs(as.getRevocationEndpointJWSAlgs());
		
		op.setRequestObjectJWSAlgs(as.getRequestObjectJWSAlgs());
		op.setRequestObjectJWEAlgs(as.getRequestObjectJWEAlgs());
		op.setRequestObjectJWEEncs(as.getRequestObjectJWEEncs());
		
		op.setSupportsRequestParam(as.supportsRequestParam());
		op.setSupportsRequestURIParam(as.supportsRequestURIParam());
		op.setRequiresRequestURIRegistration(as.requiresRequestURIRegistration());
		
		op.setCodeChallengeMethods(as.getCodeChallengeMethods());

		if (jsonObject.get("acr_values_supported") != null) {

			op.acrValues = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "acr_values_supported")) {

				if (v != null)
					op.acrValues.add(new ACR(v));
			}
		}
		
		// ID token

		if (jsonObject.get("id_token_signing_alg_values_supported") != null) {

			op.idTokenJWSAlgs = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "id_token_signing_alg_values_supported")) {

				if (v != null)
					op.idTokenJWSAlgs.add(JWSAlgorithm.parse(v));
			}
		}


		if (jsonObject.get("id_token_encryption_alg_values_supported") != null) {

			op.idTokenJWEAlgs = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "id_token_encryption_alg_values_supported")) {

				if (v != null)
					op.idTokenJWEAlgs.add(JWEAlgorithm.parse(v));
			}
		}


		if (jsonObject.get("id_token_encryption_enc_values_supported") != null) {

			op.idTokenJWEEncs = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "id_token_encryption_enc_values_supported")) {

				if (v != null)
					op.idTokenJWEEncs.add(EncryptionMethod.parse(v));
			}
		}

		// UserInfo

		if (jsonObject.get("userinfo_signing_alg_values_supported") != null) {

			op.userInfoJWSAlgs = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "userinfo_signing_alg_values_supported")) {

				if (v != null)
					op.userInfoJWSAlgs.add(JWSAlgorithm.parse(v));
			}
		}


		if (jsonObject.get("userinfo_encryption_alg_values_supported") != null) {

			op.userInfoJWEAlgs = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "userinfo_encryption_alg_values_supported")) {

				if (v != null)
					op.userInfoJWEAlgs.add(JWEAlgorithm.parse(v));
			}
		}


		if (jsonObject.get("userinfo_encryption_enc_values_supported") != null) {

			op.userInfoJWEEncs = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "userinfo_encryption_enc_values_supported")) {

					if (v != null)
						op.userInfoJWEEncs.add(EncryptionMethod.parse(v));
			}
		}

		
		// Misc

		if (jsonObject.get("display_values_supported") != null) {

			op.displays = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "display_values_supported")) {

				if (v != null)
					op.displays.add(Display.parse(v));
			}
		}
		
		if (jsonObject.get("claim_types_supported") != null) {
			
			op.claimTypes = new ArrayList<>();
			
			for (String v: JSONObjectUtils.getStringArray(jsonObject, "claim_types_supported")) {
				
				if (v != null)
					op.claimTypes.add(ClaimType.parse(v));
			}
		}


		if (jsonObject.get("claims_supported") != null) {

			op.claims = new ArrayList<>();

			for (String v: JSONObjectUtils.getStringArray(jsonObject, "claims_supported")) {

				if (v != null)
					op.claims.add(v);
			}
		}
		
		if (jsonObject.get("claims_locales_supported") != null) {
			
			op.claimsLocales = new ArrayList<>();
			
			for (String v : JSONObjectUtils.getStringArray(jsonObject, "claims_locales_supported")) {
				
				if (v != null) {
					
					try {
						op.claimsLocales.add(LangTag.parse(v));
					
					} catch (LangTagException e) {
						
						throw new ParseException("Invalid claims_locales_supported field: " + e.getMessage(), e);
					}
				}
			}
		}
		
		op.setUILocales(as.getUILocales());
		op.setServiceDocsURI(as.getServiceDocsURI());
		op.setPolicyURI(as.getPolicyURI());
		op.setTermsOfServiceURI(as.getTermsOfServiceURI());
		
		if (jsonObject.get("claims_parameter_supported") != null)
			op.claimsParamSupported = JSONObjectUtils.getBoolean(jsonObject, "claims_parameter_supported");
		
		if (jsonObject.get("request_uri_parameter_supported") == null) {
			op.setSupportsRequestURIParam(true);
		}
		
		// Optional front and back-channel logout
		if (jsonObject.get("frontchannel_logout_supported") != null)
			op.frontChannelLogoutSupported = JSONObjectUtils.getBoolean(jsonObject, "frontchannel_logout_supported");
		
		if (op.frontChannelLogoutSupported && jsonObject.get("frontchannel_logout_session_supported") != null)
			op.frontChannelLogoutSessionSupported = JSONObjectUtils.getBoolean(jsonObject, "frontchannel_logout_session_supported");
		
		if (jsonObject.get("backchannel_logout_supported") != null)
			op.backChannelLogoutSupported = JSONObjectUtils.getBoolean(jsonObject, "backchannel_logout_supported");
		
		if (op.frontChannelLogoutSupported && jsonObject.get("backchannel_logout_session_supported") != null)
			op.backChannelLogoutSessionSupported = JSONObjectUtils.getBoolean(jsonObject, "backchannel_logout_session_supported");
		
		if (jsonObject.get("mtls_endpoint_aliases") != null)
			op.setMtlsEndpointAliases(OIDCProviderEndpointMetadata.parse(JSONObjectUtils.getJSONObject(jsonObject, "mtls_endpoint_aliases")));
		
		op.setSupportsTLSClientCertificateBoundAccessTokens(as.supportsTLSClientCertificateBoundAccessTokens());
		
		// JARM
		op.setAuthorizationJWSAlgs(as.getAuthorizationJWSAlgs());
		op.setAuthorizationJWEAlgs(as.getAuthorizationJWEAlgs());
		op.setAuthorizationJWEEncs(as.getAuthorizationJWEEncs());
		
		// OpenID Connect for Identity Assurance 1.0
		if (jsonObject.get("verified_claims_supported") != null) {
			op.verifiedClaimsSupported = JSONObjectUtils.getBoolean(jsonObject, "verified_claims_supported");
			if (op.verifiedClaimsSupported) {
				if (jsonObject.get("trust_frameworks_supported") != null) {
					op.trustFrameworks = new LinkedList<>();
					for (String v : JSONObjectUtils.getStringList(jsonObject, "trust_frameworks_supported")) {
						op.trustFrameworks.add(new IdentityTrustFramework(v));
					}
				}
				if (jsonObject.get("evidence_supported") != null) {
					op.evidenceTypes = new LinkedList<>();
					for (String v: JSONObjectUtils.getStringList(jsonObject, "evidence_supported")) {
						op.evidenceTypes.add(new IdentityEvidenceType(v));
					}
				}
				if (jsonObject.get("id_documents_supported") != null) {
					op.idDocuments = new LinkedList<>();
					for (String v: JSONObjectUtils.getStringList(jsonObject, "id_documents_supported")) {
						op.idDocuments.add(new IDDocumentType(v));
					}
				}
				if (jsonObject.get("id_documents_verification_methods_supported") != null) {
					op.idVerificationMethods = new LinkedList<>();
					for (String v: JSONObjectUtils.getStringList(jsonObject, "id_documents_verification_methods_supported")) {
						op.idVerificationMethods.add(new IdentityVerificationMethod(v));
					}
				}
				if (jsonObject.get("claims_in_verified_claims_supported") != null) {
					op.verifiedClaims = JSONObjectUtils.getStringList(jsonObject, "claims_in_verified_claims_supported");
				}
			}
		}
		
		// Federation
		
		if (jsonObject.get("client_registration_types_supported") != null) {
			op.clientRegistrationTypes = new LinkedList<>();
			for (String v: JSONObjectUtils.getStringList(jsonObject, "client_registration_types_supported")) {
				op.clientRegistrationTypes.add(new ClientRegistrationType(v));
			}
		}
		
		if (jsonObject.get("client_registration_authn_methods_supported") != null) {
			Map<String,List<ClientAuthenticationMethod>> fedClientAuthMethods = new HashMap<>();
			JSONObject spec = JSONObjectUtils.getJSONObject(jsonObject, "client_registration_authn_methods_supported");
			// ar or rar
			for (String requestType: spec.keySet()) {
				List<String> methodNames = JSONObjectUtils.getStringList(spec, requestType, Collections.<String>emptyList());
				List<ClientAuthenticationMethod> authMethods = new LinkedList<>();
				for (String name: methodNames) {
					authMethods.add(ClientAuthenticationMethod.parse(name));
				}
				fedClientAuthMethods.put(requestType, authMethods);
			}
			op.setClientRegistrationAuthnMethods(fedClientAuthMethods);
		}
		
		op.organizationName = JSONObjectUtils.getString(jsonObject, "organization_name", null);
		
		op.federationRegistrationEndpoint = JSONObjectUtils.getURI(jsonObject, "federation_registration_endpoint", null);
		
		// Parse custom (not registered) parameters
		for (Map.Entry<String,?> entry: as.getCustomParameters().entrySet()) {
			if (REGISTERED_PARAMETER_NAMES.contains(entry.getKey()))
				continue; // skip
			op.setCustomParameter(entry.getKey(), entry.getValue());
		}

		return op;
	}


	/**
	 * Parses an OpenID Provider metadata from the specified JSON object
	 * string.
	 *
	 * @param s The JSON object sting to parse. Must not be {@code null}.
	 *
	 * @return The OpenID Provider metadata.
	 *
	 * @throws ParseException If the JSON object string couldn't be parsed
	 *                        to an OpenID Provider metadata.
	 */
	public static OIDCProviderMetadata parse(final String s)
		throws ParseException {

		return parse(JSONObjectUtils.parse(s));
	}
	
	
	/**
	 * Resolves OpenID Provider metadata URL from the specified issuer
	 * identifier.
	 *
	 * @param issuer The OpenID Provider issuer identifier. Must represent
	 *               a valid HTTPS or HTTP URL. Must not be {@code null}.
	 *
	 * @return The OpenID Provider metadata URL.
	 *
	 * @throws GeneralException If the issuer identifier is invalid.
	 */
	public static URL resolveURL(final Issuer issuer)
		throws GeneralException {
		
		try {
			URL issuerURL = new URL(issuer.getValue());
			
			// Validate but don't insist on HTTPS, see
			// http://openid.net/specs/openid-connect-core-1_0.html#Terminology
			if (issuerURL.getQuery() != null && ! issuerURL.getQuery().trim().isEmpty()) {
				throw new GeneralException("The issuer identifier must not contain a query component");
			}
			
			if (issuerURL.getPath() != null && issuerURL.getPath().endsWith("/")) {
				return new URL(issuerURL + ".well-known/openid-configuration");
			} else {
				return new URL(issuerURL + "/.well-known/openid-configuration");
			}
			
		} catch (MalformedURLException e) {
			throw new GeneralException("The issuer identifier doesn't represent a valid URL: " + e.getMessage(), e);
		}
	}
	
	
	/**
	 * Resolves OpenID Provider metadata from the specified issuer
	 * identifier. The metadata is downloaded by HTTP GET from
	 * {@code [issuer-url]/.well-known/openid-configuration}.
	 *
	 * @param issuer The OpenID Provider issuer identifier. Must represent
	 *               a valid HTTPS or HTTP URL. Must not be {@code null}.
	 *
	 * @return The OpenID Provider metadata.
	 *
	 * @throws GeneralException If the issuer identifier or the downloaded
	 *                          metadata are invalid.
	 * @throws IOException      On a HTTP exception.
	 */
	public static OIDCProviderMetadata resolve(final Issuer issuer)
		throws GeneralException, IOException {
		
		return resolve(issuer, 0, 0);
	}
	
	
	/**
	 * Resolves OpenID Provider metadata from the specified issuer
	 * identifier. The metadata is downloaded by HTTP GET from
	 * {@code [issuer-url]/.well-known/openid-configuration}, using the
	 * specified HTTP timeouts.
	 *
	 * @param issuer         The issuer identifier. Must represent a valid
	 *                       HTTPS or HTTP URL. Must not be {@code null}.
	 * @param connectTimeout The HTTP connect timeout, in milliseconds.
	 *                       Zero implies no timeout. Must not be negative.
	 * @param readTimeout    The HTTP response read timeout, in
	 *                       milliseconds. Zero implies no timeout. Must
	 *                       not be negative.
	 *
	 * @return The OpenID Provider metadata.
	 *
	 * @throws GeneralException If the issuer identifier or the downloaded
	 *                          metadata are invalid.
	 * @throws IOException      On a HTTP exception.
	 */
	public static OIDCProviderMetadata resolve(final Issuer issuer,
						   final int connectTimeout,
						   final int readTimeout)
		throws GeneralException, IOException {
		
		URL configURL = resolveURL(issuer);
		
		HTTPRequest httpRequest = new HTTPRequest(HTTPRequest.Method.GET, configURL);
		httpRequest.setConnectTimeout(connectTimeout);
		httpRequest.setReadTimeout(readTimeout);
		
		HTTPResponse httpResponse = httpRequest.send();
		
		if (httpResponse.getStatusCode() != 200) {
			throw new IOException("Couldn't download OpenID Provider metadata from " + configURL +
				": Status code " + httpResponse.getStatusCode());
		}
		
		JSONObject jsonObject = httpResponse.getContentAsJSONObject();
		
		OIDCProviderMetadata op = OIDCProviderMetadata.parse(jsonObject);
		
		if (! issuer.equals(op.getIssuer())) {
			throw new GeneralException("The returned issuer doesn't match the expected: " + op.getIssuer());
		}
		
		return op;
	}
}