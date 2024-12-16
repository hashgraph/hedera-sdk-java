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
package com.hiero.tck.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JRPCInterceptorTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Test
    void testMapToJSONRPC2Request() throws Exception {
        // given
        String requestBody =
                "{\"jsonrpc\": \"2.0\", \"method\": \"testMethod\", \"params\": {\"param1\": \"value1\"}, \"id\": 1}";
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        JRPCInterceptor interceptor = new JRPCInterceptor();

        // when
        JSONRPC2Request jsonrpcRequest = interceptor.mapToJSONRPC2Request(request);

        // then
        assertNotNull(jsonrpcRequest);
        assertEquals("testMethod", jsonrpcRequest.getMethod());
        assertEquals(1L, jsonrpcRequest.getID());
    }

    @Test
    void testPreHandle() throws Exception {
        // given
        String requestBody =
                "{\"jsonrpc\": \"2.0\", \"method\": \"testMethod\", \"params\": {\"param1\": \"value1\"}, \"id\": 1}";
        BufferedReader reader = new BufferedReader(new StringReader(requestBody));
        when(request.getReader()).thenReturn(reader);

        JRPCInterceptor interceptor = new JRPCInterceptor();

        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertTrue(result);
        verify(request).setAttribute(eq("jsonrpcRequest"), any(JSONRPC2Request.class));
    }
}
