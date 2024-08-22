/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.tck.methods.sdk.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SetupResponseTest {

    @Test
    void testConstructorWithMessage() {
        // Given
        String message = "Test message";

        // When
        SetupResponse setupResponse = new SetupResponse(message);

        // Then
        assertNotNull(setupResponse);
        assertEquals(message, setupResponse.getMessage());
        assertEquals("SUCCESS", setupResponse.getStatus());
    }

    @Test
    void testConstructorWithNullMessage() {
        // Given
        String message = null;

        // When
        SetupResponse setupResponse = new SetupResponse(message);

        // Then
        assertNotNull(setupResponse);
        assertEquals("", setupResponse.getMessage()); // message should default to empty string
        assertEquals("SUCCESS", setupResponse.getStatus());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        // Given
        String message = "";

        // When
        SetupResponse setupResponse = new SetupResponse(message);

        // Then
        assertNotNull(setupResponse);
        assertEquals("", setupResponse.getMessage());
        assertEquals("SUCCESS", setupResponse.getStatus());
    }
}
