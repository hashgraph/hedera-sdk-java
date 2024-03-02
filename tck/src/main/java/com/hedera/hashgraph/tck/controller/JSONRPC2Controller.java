package com.hedera.hashgraph.tck.controller;

import static com.hedera.hashgraph.tck.util.JSONRPC2ServiceScanner.registerServices;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JSONRPC2Controller {
    private final Dispatcher dispatcher;

    public JSONRPC2Controller(final Dispatcher dispatcher, final ApplicationContext applicationContext) {
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
        return resp.toJSONString();
    }
}
