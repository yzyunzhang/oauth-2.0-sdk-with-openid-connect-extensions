/*
 * oauth2-oidc-sdk
 *
 * Copyright 2012-2020, Connect2id Ltd and contributors.
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

package com.nimbusds.openid.connect.sdk.federation.policy;


import java.net.URI;
import java.util.*;

import junit.framework.TestCase;
import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.federation.policy.language.PolicyOperation;
import com.nimbusds.openid.connect.sdk.federation.policy.language.PolicyViolationException;
import com.nimbusds.openid.connect.sdk.federation.policy.operations.*;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;


public class MetadataPolicyTest extends TestCase {
	
	
	public void testEmpty() throws ParseException, PolicyViolationException {
		
		MetadataPolicy metadataPolicy = new MetadataPolicy();
		
		assertNull(metadataPolicy.get("no-such-parameter"));
		assertNull(metadataPolicy.getEntry("no-such-parameter"));
		
		assertTrue(metadataPolicy.entrySet().isEmpty());
		
		assertNull(metadataPolicy.remove("no-such-parameter"));
		
		assertTrue(metadataPolicy.toJSONObject().isEmpty());
		assertEquals("{}", metadataPolicy.toJSONString());
		
		metadataPolicy = MetadataPolicy.parse("{}");
		
		assertTrue(metadataPolicy.entrySet().isEmpty());
	}
	
	
	public void testWithOneEntry() throws ParseException, PolicyViolationException {
		
		MetadataPolicy metadataPolicy = new MetadataPolicy();
		
		String parameterName = "id_token_signing_alg";
		OneOfOperation op = new OneOfOperation();
		op.configure(Arrays.asList("RS256", "RS384", "RS512"));
		
		// put
		metadataPolicy.put(parameterName, op);
		
		assertEquals(Collections.singletonList(op), metadataPolicy.get(parameterName));
		
		assertEquals(parameterName, metadataPolicy.getEntry(parameterName).getParameterName());
		assertEquals(Collections.singletonList(op), metadataPolicy.getEntry(parameterName).getPolicyOperations());
		
		assertEquals(1, metadataPolicy.entrySet().size());
		
		// remove
		assertEquals(Collections.singletonList(op), metadataPolicy.remove(parameterName));
		
		assertTrue(metadataPolicy.entrySet().isEmpty());
		
		// put back in
		metadataPolicy.put(parameterName, op);
		
		Map<String,Object> jsonObject = metadataPolicy.toJSONObject();
		
		String json = JSONObject.toJSONString(jsonObject);
		assertEquals("{\"id_token_signing_alg\":{\"one_of\":[\"RS256\",\"RS384\",\"RS512\"]}}", json);
		
		metadataPolicy = MetadataPolicy.parse(json);
		
		assertEquals(op.getOperationName(), metadataPolicy.getEntry(parameterName).getPolicyOperations().get(0).getOperationName());
		assertEquals(op.getStringListConfiguration(), ((OneOfOperation)metadataPolicy.getEntry(parameterName).getPolicyOperations().get(0)).getStringListConfiguration());
		
		assertEquals(1, metadataPolicy.entrySet().size());
		
		Iterator<MetadataPolicyEntry> it = metadataPolicy.entrySet().iterator();
		MetadataPolicyEntry en = it.next();
		assertEquals(parameterName, en.getParameterName());
		assertEquals(op.getOperationName(), en.getPolicyOperations().get(0).getOperationName());
		assertEquals(op.getStringListConfiguration(), ((OneOfOperation)en.getPolicyOperations().get(0)).getStringListConfiguration());
		assertFalse(it.hasNext());
	}
	
	
	public void testExample() throws ParseException, PolicyViolationException {
		
		String json = "{"+
			"\"scopes\":{"+
			"\"subset_of\":[\"openid\",\"eduperson\",\"phone\"],"+
			"\"superset_of\":[\"openid\"],"+
			"\"default\":[\"openid\",\"eduperson\"]},"+
			"\"id_token_signed_response_alg\":{"+
			"\"one_of\":[\"ES256\",\"ES384\",\"ES512\"]},"+
			"\"contacts\":{"+
			"\"add\":\"helpdesk@federation.example.org\"},"+
			"\"application_type\":{\"value\":\"web\"}"+
			"}";
		
		MetadataPolicy metadataPolicy = MetadataPolicy.parse(json);
		
		// scopes
		MetadataPolicyEntry en = metadataPolicy.getEntry("scopes");
		assertEquals("scopes", en.getParameterName());
		List<PolicyOperation> ops = en.getPolicyOperations();
		assertEquals(3, ops.size());
		
		for (PolicyOperation op: ops) {
			
			if (op instanceof SubsetOfOperation) {
				
				SubsetOfOperation subsetOfOperation = (SubsetOfOperation)op;
				assertEquals(Arrays.asList("openid", "eduperson", "phone"), subsetOfOperation.getStringListConfiguration());
				
			} else if (op instanceof SupersetOfOperation) {
				
				SupersetOfOperation supersetOfOperation = (SupersetOfOperation)op;
				assertEquals(Collections.singletonList("openid"), supersetOfOperation.getStringListConfiguration());
				
			} else if (op instanceof DefaultOperation) {
				
				DefaultOperation defaultOperation = (DefaultOperation)op;
				assertEquals(Arrays.asList("openid", "eduperson"), defaultOperation.getStringListConfiguration());
				
			} else {
				fail();
			}
		}
		
		// id_token_signed_response_alg
		en = metadataPolicy.getEntry("id_token_signed_response_alg");
		assertEquals("id_token_signed_response_alg", en.getParameterName());
		ops = en.getPolicyOperations();
		
		OneOfOperation oneOfOperation = (OneOfOperation) ops.get(0);
		assertEquals(Arrays.asList("ES256", "ES384", "ES512"), oneOfOperation.getStringListConfiguration());
		
		assertEquals(1, ops.size());
		
		// contacts
		en = metadataPolicy.getEntry("contacts");
		assertEquals("contacts", en.getParameterName());
		ops = en.getPolicyOperations();
		
		AddOperation addOperation = (AddOperation) ops.get(0);
		assertEquals("helpdesk@federation.example.org", addOperation.getStringConfiguration());
		
		assertEquals(1, ops.size());
		
		// application_type
		en = metadataPolicy.getEntry("application_type");
		assertEquals("application_type", en.getParameterName());
		ops = en.getPolicyOperations();
		
		ValueOperation valueOperation = (ValueOperation) ops.get(0);
		assertEquals("web", valueOperation.getStringConfiguration());
		
		assertEquals(1, ops.size());
		
		// Iterator test
		Iterator<MetadataPolicyEntry> it = metadataPolicy.entrySet().iterator();
		
		Set<String> paramNamesToIterate = new HashSet<>(Arrays.asList("scopes", "id_token_signed_response_alg", "contacts", "application_type"));
		while (it.hasNext()) {
			MetadataPolicyEntry entry = it.next();
			paramNamesToIterate.remove(entry.getParameterName());
		}
		assertTrue(paramNamesToIterate.isEmpty());
		
		// Back to JSON object
		assertEquals(JSONObjectUtils.parse(json), metadataPolicy.toJSONObject());
	}
	
	
	public void testApply_copyOnly() throws PolicyViolationException {
		
		
		MetadataPolicy policy = new MetadataPolicy();
		
		OIDCClientMetadata metadata = new OIDCClientMetadata();
		metadata.setRedirectionURI(URI.create("https://example.com/cb"));
		metadata.setEmailContacts(Collections.singletonList("admin@example.com"));
		metadata.applyDefaults();
		
		assertEquals(metadata.toJSONObject(), policy.apply(metadata.toJSONObject()));
	}
	
	
	public void testApply_copyAndApply() throws PolicyViolationException {
		
		
		MetadataPolicy policy = new MetadataPolicy();
		DefaultOperation defaultOperation = new DefaultOperation();
		defaultOperation.configure(URI.create("https://example.com/policy.html").toString());
		policy.put("policy_uri", defaultOperation);
		
		OIDCClientMetadata metadata = new OIDCClientMetadata();
		metadata.setRedirectionURI(URI.create("https://example.com/cb"));
		metadata.setEmailContacts(Collections.singletonList("admin@example.com"));
		metadata.applyDefaults();
		JSONObject in = metadata.toJSONObject();
		
		metadata.setPolicyURI(URI.create("https://example.com/policy.html"));
		JSONObject expectedOut = metadata.toJSONObject();
		
		assertEquals(expectedOut, policy.apply(in));
	}
	
	
	// https://openid.net/specs/openid-connect-federation-1_0.html#rfc.section.4.1.6
	public void testApply_example() throws ParseException, PolicyViolationException {
		
		String policyJSON = "{" +
			"  \"contacts\": {" +
			"    \"add\": \"helpdesk@example.com\"" +
			"  }," +
			"  \"logo_uri\": {" +
			"    \"one_of\": [" +
			"      \"https://example.com/logo_small.jpg\"," +
			"      \"https://example.com/logo_big.jpg\"" +
			"    ]," +
			"    \"default\": \"https://example.com/logo_small.jpg\"" +
			"  }," +
			"  \"policy_uri\": {" +
			"    \"value\": \"https://example.com/policy.html\"" +
			"  }," +
			"  \"tos_uri\": {" +
			"    \"value\": \"https://example.com/tos.html\"" +
			"  }" +
			"}";
		
		String rpMetadataJSON = "{" +
			"  \"contacts\": [" +
			"    \"rp_admins@cs.example.com\"" +
			"  ]," +
			"  \"redirect_uris\": [" +
			"    \"https://cs.example.com/rp1\"" +
			"  ]," +
			"  \"response_types\": [" +
			"    \"code\"" +
			"  ]" +
			"}";
		
		String expectedEndMetadataJSON = "{" +
			"  \"contacts\": [" +
			"    \"rp_admins@cs.example.com\"," +
			"    \"helpdesk@example.com\"" +
			"  ]," +
			"  \"logo_uri\": \"https://example.com/logo_small.jpg\"," +
			"  \"policy_uri\": \"https://example.com/policy.html\"," +
			"  \"tos_uri\": \"https://example.com/tos.html\"," +
// TODO bug?		"  \"scopes\": [" +
//			"    \"openid\"," +
//			"    \"eduperson\"" +
//			"  ]," +
			"  \"response_types\": [" +
			"    \"code\"" +
			"  ]," +
			"  \"redirect_uris\": [" +
			"    \"https://cs.example.com/rp1\"" +
			"  ]" +
			"}";
		
		MetadataPolicy policy = MetadataPolicy.parse(policyJSON);
		
		JSONObject metadata = JSONObjectUtils.parse(rpMetadataJSON);
		
		JSONObject endMetadata = policy.apply(metadata);
		
		JSONObject expectedEndMetadata = JSONObjectUtils.parse(expectedEndMetadataJSON);
		
		assertEquals(expectedEndMetadata, endMetadata);
	}
}
