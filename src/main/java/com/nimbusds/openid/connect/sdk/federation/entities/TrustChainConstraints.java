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


import java.util.LinkedList;
import java.util.List;

import net.jcip.annotations.Immutable;
import net.minidev.json.JSONAware;
import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.Identifier;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;


/**
 * Trust chain constraints.
 *
 * <p>Example JSON object:
 *
 * <pre>
 * {
 *   "max_path_length"    : 2,
 *   "naming_constraints" : {
 *   	"permitted" : [ "https://example.com" ],
 *   	"excluded"  : [ "https://east.example.com" ]
 *   }
 * }
 * </pre>
 *
 * <p>Related specifications:
 *
 * <ul>
 *     <li>OpenID Connect Federation 1.0, section 7.2.
 *     <li>RFC 5280, section 4.2.1.10.
 * </ul>
 */
@Immutable
public final class TrustChainConstraints implements JSONAware {
	
	
	/**
	 * The max path length, -1 if not specified.
	 */
	private int maxPathLength;
	
	
	/**
	 * The permitted entities.
	 */
	private List<EntityID> permittedEntities;
	
	
	/**
	 * The excluded entities.
	 */
	private List<EntityID> excludedEntities;
	
	
	/**
	 * Creates a new trust chain constraints instance.
	 *
	 * @param maxPathLength     The maximum number of entities between this
	 *                          and the last one in the chain, -1 if not
	 *                          specified.
	 * @param permittedEntities The permitted subordinate entities,
	 *                          {@code null} if not specified.
	 * @param excludedEntities  The excluded subordinate entities,
	 *                          {@code null} if not specified.
	 */
	public TrustChainConstraints(final int maxPathLength, final List<EntityID> permittedEntities, final List<EntityID> excludedEntities) {
		this.maxPathLength = maxPathLength;
		this.permittedEntities = permittedEntities;
		this.excludedEntities = excludedEntities;
	}
	
	
	/**
	 * Returns the maximum number of entities between this and the last one
	 * in the chain.
	 *
	 * @return The maximum number of entities between this and the last one
	 *         in the chain, -1 if not specified.
	 */
	public int getMaxPathLength() {
		return maxPathLength;
	}
	
	
	/**
	 * Returns the permitted subordinate entities, {@code null} if not
	 * specified.
	 *
	 * @return The permitted subordinate entities, {@code null} if not
	 *         specified.
	 */
	public List<EntityID> getPermittedEntities() {
		return permittedEntities;
	}
	
	
	/**
	 * Returns the excluded subordinate entities, {@code null} if not
	 * specified.
	 *
	 * @return The excluded subordinate entities, {@code null} if not
	 *         specified.
	 */
	public List<EntityID> getExcludedEntities() {
		return excludedEntities;
	}
	
	
	/**
	 * Returns a JSON object representation of this trust chain
	 * constraints.
	 *
	 * @return The JSON object.
	 */
	public JSONObject toJSONObject() {
	
		JSONObject o = new JSONObject();
		
		if (maxPathLength > -1) {
			o.put("max_path_length", maxPathLength);
		}
		
		JSONObject namingConstraints = new JSONObject();
		
		if (CollectionUtils.isNotEmpty(permittedEntities)) {
			namingConstraints.put("permitted", Identifier.toStringList(permittedEntities));
		}
		
		if (CollectionUtils.isNotEmpty(excludedEntities)) {
			namingConstraints.put("excluded", Identifier.toStringList(excludedEntities));
		}
		
		if (! namingConstraints.isEmpty()) {
			o.put("naming_constraints", namingConstraints);
		}
		
		return o;
	}
	
	
	@Override
	public String toJSONString() {
		return toJSONObject().toJSONString();
	}
	
	
	/**
	 * Parses a trust chain constraints instance from the specified JSON
	 * object.
	 *
	 * @param jsonObject The JSON object. Must not be {@code null}.
	 *
	 * @return The trust chain constraints.
	 *
	 * @throws ParseException If parsing failed.
	 */
	public static TrustChainConstraints parse(final JSONObject jsonObject)
		throws ParseException {
		
		int maxPathLength = JSONObjectUtils.getInt(jsonObject, "max_path_length", -1);
		
		JSONObject namingConstraints = JSONObjectUtils.getJSONObject(jsonObject, "naming_constraints", new JSONObject());
		
		List<EntityID> permitted = null;
		List<String> values = JSONObjectUtils.getStringList(namingConstraints, "permitted", null);
		if (values != null) {
			permitted = new LinkedList<>();
			for (String v: values) {
				if (v != null) {
					permitted.add(new EntityID(v));
				}
			}
		}
		
		List<EntityID> excluded = null;
		values = JSONObjectUtils.getStringList(namingConstraints, "excluded", null);
		if (values != null) {
			excluded = new LinkedList<>();
			for (String v: values) {
				if (v != null) {
					excluded.add(new EntityID(v));
				}
			}
		}
		
		return new TrustChainConstraints(maxPathLength, permitted, excluded);
	}
}
