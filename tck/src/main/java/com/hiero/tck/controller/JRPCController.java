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

import static com.hiero.tck.util.JSONRPC2ServiceScanner.registerServices;

import com.hiero.tck.annotation.JSONRPC2Controller;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;

@JSONRPC2Controller
public class JRPCController {
    private final Logger logger = LoggerFactory.getLogger(JRPCController.class);
    private final Dispatcher dispatcher;

    public JRPCController(final Dispatcher dispatcher, final ApplicationContext applicationContext) {
        this.dispatcher = dispatcher;
        registerServices(dispatcher, applicationContext);
    }

    /**
     * Endpoint to handle all incoming JSON-RPC requests
     */
    @PostMapping("/")
    public String handleJSONRPC2Request(final HttpServletRequest request) {
        var req = (JSONRPC2Request) request.getAttribute("jsonrpcRequest");
        var resp = dispatcher.process(req, null);

        if (resp.getError() != null) {
            String errorMessage = String.format(
                    "Error occurred processing JSON-RPC request: %s, Response error: %s",
                    req.toJSONString(), resp.getError().toString());
            logger.info(errorMessage);
        }

        return resp.toJSONString();
    }
}
