// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.tck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TckServer {
    public static void main(String[] args) {
        SpringApplication.run(TckServer.class, args);
    }
}
