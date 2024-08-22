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
package com.hedera.hashgraph.tck.methods;

import static com.hedera.hashgraph.tck.methods.JSONRPC2Error.HEDERA_ERROR;

import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.exception.InvalidJSONRPC2ParamsException;
import com.hedera.hashgraph.tck.exception.InvalidJSONRPC2RequestException;
import com.hedera.hashgraph.tck.methods.JSONRPC2Error.ErrorData;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minidev.json.JSONObject;

/**
 * Implements RequestHandler and overrides some of the Dispatcher logic,
 * enhancing the usability of the process method.
 *
 * Keeps track of the {@link JSONRPC2Method} annotated methods in a map,
 * converts the JSON-RPC params to the needed args in the called method
 * and invokes it.
 */
public abstract class AbstractJSONRPC2Service implements RequestHandler {

    // this is shared state to all requests so there could be race conditions
    // although the tck driver would not call these methods in such way
    private final Map<String, Method> methodMap;

    protected AbstractJSONRPC2Service() {
        methodMap = new HashMap<>();
        registerMethods();
    }

    /**
     * Register JSONRPCMethods
     */
    private void registerMethods() {
        Method[] methods = getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(JSONRPC2Method.class)) {
                JSONRPC2Method annotation = method.getAnnotation(JSONRPC2Method.class);
                methodMap.put(annotation.value(), method);
            }
        }
    }

    @Override
    public String[] handledRequests() {
        return methodMap.keySet().toArray(new String[0]);
    }

    /**
     *
     * @param req JSON-RPC request
     * @param messageContext
     * @return JSONRPC2Response - result returned from the JSONRPCMethod or if error is thrown - JSONRPC2Error
     */
    @SuppressWarnings("java:S1874")
    @Override
    public JSONRPC2Response process(final JSONRPC2Request req, final MessageContext messageContext) {
        try {
            Method method = methodMap.get(req.getMethod());
            if (method != null) {
                Object[] args = getArguments(method, req.getNamedParams());
                Object result = method.invoke(this, args);
                return new JSONRPC2Response(result, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        } catch (InvalidJSONRPC2ParamsException e) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } catch (InvocationTargetException e) {
            // target exception can be anything
            // if its precheck, receipt - we handle it by setting error object
            // and returning custom HEDERA_STATUS_CODE -32001
            // if not - return server error or invalid request error codes
            var targetException = e.getTargetException();

            ErrorData errorData;

            if (targetException instanceof PrecheckStatusException precheckStatusException) {
                errorData = new ErrorData(precheckStatusException.status, precheckStatusException.getMessage());
            } else if (targetException instanceof ReceiptStatusException receiptStatusException) {
                errorData = new ErrorData(receiptStatusException.receipt.status, receiptStatusException.getMessage());
            } else if (targetException instanceof InvalidJSONRPC2RequestException) {
                return new JSONRPC2Response(JSONRPC2Error.INVALID_REQUEST, req.getID());
            } else {
                return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
            }
            JSONObject errorJsonObject = new JSONObject();
            errorJsonObject.put("status", errorData.status().toString());
            errorJsonObject.put("message", errorData.message());

            var hederaError = HEDERA_ERROR.setData(errorJsonObject);
            return new JSONRPC2Response(hederaError, req.getID());
        } catch (Exception e) {
            // other exceptions
            return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
        }
    }

    /**
     *
     * @param method the method that is being called
     * @param jrpcParams
     * @return parsed arguments for the method being called using reflection
     */
    private Object[] getArguments(final Method method, final Map<String, Object> jrpcParams)
            throws InvalidJSONRPC2ParamsException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            try {
                var paramInstance = paramTypes[i].newInstance();
                if (paramInstance instanceof JSONRPC2Param jsonRpcParam) {
                    args[i] = jsonRpcParam.parse(jrpcParams);
                }
            } catch (Exception e) {
                throw new InvalidJSONRPC2ParamsException("Invalid parameters for method %s with args: %s"
                        .formatted(method.getName(), Arrays.toString(args)));
            }
        }
        return args;
    }
}
