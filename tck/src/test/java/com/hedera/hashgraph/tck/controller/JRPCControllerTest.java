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
package com.hedera.hashgraph.tck.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class JRPCControllerTest {

    @Mock
    Dispatcher dispatcher;

    @Mock
    HttpServletRequest request;

    @Mock
    JSONRPC2Request jsonrpcRequest;

    @Test
    void handleJSONRPC2RequestValidRequestReturnsExpectedResponse() {
        // given
        JRPCController controller = new JRPCController(dispatcher, mock(ApplicationContext.class));
        JSONRPC2Response expectedResponse = new JSONRPC2Response("result", 1L);
        when(request.getAttribute("jsonrpcRequest")).thenReturn(jsonrpcRequest);
        when(dispatcher.process(jsonrpcRequest, null)).thenReturn(expectedResponse);

        // when
        String response = controller.handleJSONRPC2Request(request);

        // then
        verify(dispatcher).process(jsonrpcRequest, null);
        assertEquals(expectedResponse.toJSONString(), response);
    }

    @Test
    void handleJSONRPC2RequestResponseWithErrorLogsErrorMessage() {
        // given
        JRPCController controller = new JRPCController(dispatcher, mock(ApplicationContext.class));
        JSONRPC2Response errorResponse = new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, 1L);
        when(request.getAttribute("jsonrpcRequest")).thenReturn(jsonrpcRequest);
        when(dispatcher.process(jsonrpcRequest, null)).thenReturn(errorResponse);

        // when
        controller.handleJSONRPC2Request(request);

        // then
        verify(dispatcher).process(jsonrpcRequest, null);
    }
}
