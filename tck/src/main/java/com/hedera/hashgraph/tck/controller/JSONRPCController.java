package com.hedera.hashgraph.tck.controller;

import com.hedera.hashgraph.tck.methods.SdkService;
import com.hedera.hashgraph.tck.params.SetupParams;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class JSONRPCController {
    private final SdkService sdkService;

    public JSONRPCController(SdkService sdkService) {
        this.sdkService = sdkService;
    }

    @PostMapping("/")
    public SetupParams setup(@RequestBody SetupParams setupParams) {
        // You can process the setupParams object here if needed
        return setupParams;
    }
}
