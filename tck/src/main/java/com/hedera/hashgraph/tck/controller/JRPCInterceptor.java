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

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor class to map HttpServletRequest body to {@link JSONRPC2Request}
 * before the request reaches the controller
 */
public class JRPCInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {
        // Map HTTP Servlet Request to JSON-RPC Request
        var jsonrpcRequest = mapToJSONRPC2Request(request);

        // Store the JSON-RPC request in request attribute for further processing
        request.setAttribute("jsonrpcRequest", jsonrpcRequest);

        // Continue processing the request
        return true;
    }

    JSONRPC2Request mapToJSONRPC2Request(final HttpServletRequest httpRequest)
            throws IOException, JSONRPC2ParseException {
        // Read the JSON-RPC request from the HTTP request body
        BufferedReader reader = httpRequest.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        reader.close();

        // Parse the JSON-RPC request
        return JSONRPC2Request.parse(requestBody.toString());
    }
}
