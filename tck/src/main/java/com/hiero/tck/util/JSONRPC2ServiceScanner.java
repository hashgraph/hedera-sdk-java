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
package com.hiero.tck.util;

import com.hiero.tck.annotation.JSONRPC2Service;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.springframework.context.ApplicationContext;

/**
 * Utility class to register all {@link JSONRPC2Service} annotated classes
 * to the JSON-RPC dispatcher
 */
public class JSONRPC2ServiceScanner {
    private JSONRPC2ServiceScanner() {
        // private constructor for utility class
    }

    public static void registerServices(Dispatcher dispatcher, ApplicationContext context) {
        String[] serviceNames = context.getBeanNamesForAnnotation(JSONRPC2Service.class);
        if (serviceNames != null) { // NOSONAR
            for (String serviceName : serviceNames) {
                Object service = context.getBean(serviceName);
                dispatcher.register((RequestHandler) service);
            }
        }
    }
}
