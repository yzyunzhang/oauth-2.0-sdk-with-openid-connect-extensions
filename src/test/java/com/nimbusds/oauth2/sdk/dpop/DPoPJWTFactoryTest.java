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

package com.nimbusds.oauth2.sdk.dpop;


import junit.framework.TestCase;

import com.nimbusds.jose.JOSEObjectType;


public class DPoPJWTFactoryTest extends TestCase {
	
	
	public void testConstants() {
		
		assertEquals(new JOSEObjectType("dpop+jwt"), DPoPJWTFactory.TYPE);
		
		assertEquals(96 / 8, DPoPJWTFactory.MINIMAL_JTI_BYTE_LENGTH);
	}
}
