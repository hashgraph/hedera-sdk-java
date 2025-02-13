// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.tck.config;

import com.hedera.hashgraph.tck.controller.JRPCInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JRPCInterceptor());
    }
}
