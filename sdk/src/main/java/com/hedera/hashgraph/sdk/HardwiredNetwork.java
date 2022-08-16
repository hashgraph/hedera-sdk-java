package com.hedera.hashgraph.sdk;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class HardwiredNetwork {

    /**
     * Create a mainnet network.
     *
     * @param executor                  the executor service
     * @return                          the new mainnet network
     */
    static Network forMainnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();

        network.put("13.124.142.126:50211", AccountId.fromString("0.0.3"));
        network.put("15.164.44.66:50211", AccountId.fromString("0.0.3"));
        network.put("15.165.118.251:50211", AccountId.fromString("0.0.3"));
        network.put("34.239.82.6:50211", AccountId.fromString("0.0.3"));
        network.put("35.237.200.180:50211", AccountId.fromString("0.0.3"));
        network.put("3.130.52.236:50211", AccountId.fromString("0.0.4"));
        network.put("35.186.191.247:50211", AccountId.fromString("0.0.4"));
        network.put("107.155.64.98:50211", AccountId.fromString("0.0.5"));
        network.put("23.111.186.250:50211", AccountId.fromString("0.0.5"));
        network.put("3.18.18.254:50211", AccountId.fromString("0.0.5"));
        network.put("35.192.2.25:50211", AccountId.fromString("0.0.5"));
        network.put("74.50.117.35:50211", AccountId.fromString("0.0.5"));
        network.put("104.211.205.124:50211", AccountId.fromString("0.0.6"));
        network.put("13.235.15.32:50211", AccountId.fromString("0.0.6"));
        network.put("13.52.108.243:50211", AccountId.fromString("0.0.6"));
        network.put("13.71.90.154:50211", AccountId.fromString("0.0.6"));
        network.put("35.199.161.108:50211", AccountId.fromString("0.0.6"));
        network.put("3.114.54.4:50211", AccountId.fromString("0.0.7"));
        network.put("35.203.82.240:50211", AccountId.fromString("0.0.7"));
        network.put("35.183.66.150:50211", AccountId.fromString("0.0.8"));
        network.put("35.236.5.219:50211", AccountId.fromString("0.0.8"));
        network.put("35.181.158.250:50211", AccountId.fromString("0.0.9"));
        network.put("35.197.192.225:50211", AccountId.fromString("0.0.9"));
        network.put("179.190.33.184:50211", AccountId.fromString("0.0.10"));
        network.put("3.248.27.48:50211", AccountId.fromString("0.0.10"));
        network.put("35.242.233.154:50211", AccountId.fromString("0.0.10"));
        network.put("13.53.119.185:50211", AccountId.fromString("0.0.11"));
        network.put("35.240.118.96:50211", AccountId.fromString("0.0.11"));
        network.put("69.87.221.231:50211", AccountId.fromString("0.0.11"));
        network.put("69.87.222.61:50211", AccountId.fromString("0.0.11"));
        network.put("96.126.72.172:50211", AccountId.fromString("0.0.11"));
        network.put("35.177.162.180:50211", AccountId.fromString("0.0.12"));
        network.put("35.204.86.32:50211", AccountId.fromString("0.0.12"));

        return new Network(executor, network).setLedgerId(LedgerId.MAINNET);
    }

    /**
     * Create a testnet network.
     *
     * @param executor                  the executor service
     * @return                          the new testnet network
     */
    static Network forTestnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();

        network.put("138.91.142.219:50211", AccountId.fromString("0.0.3"));
        network.put("34.94.106.61:50211", AccountId.fromString("0.0.3"));
        network.put("50.18.132.211:50211", AccountId.fromString("0.0.3"));
        network.put("3.212.6.13:50211", AccountId.fromString("0.0.4"));
        network.put("35.237.119.55:50211", AccountId.fromString("0.0.4"));
        network.put("52.168.76.241:50211", AccountId.fromString("0.0.4"));
        network.put("35.245.27.193:50211", AccountId.fromString("0.0.5"));
        network.put("40.79.83.124:50211", AccountId.fromString("0.0.5"));
        network.put("52.20.18.86:50211", AccountId.fromString("0.0.5"));
        network.put("34.83.112.116:50211", AccountId.fromString("0.0.6"));
        network.put("52.183.45.65:50211", AccountId.fromString("0.0.6"));
        network.put("54.70.192.33:50211", AccountId.fromString("0.0.6"));
        network.put("13.64.181.136:50211", AccountId.fromString("0.0.7"));
        network.put("34.94.160.4:50211", AccountId.fromString("0.0.7"));
        network.put("54.176.199.109:50211", AccountId.fromString("0.0.7"));
        network.put("13.78.238.32:50211", AccountId.fromString("0.0.8"));
        network.put("34.106.102.218:50211", AccountId.fromString("0.0.8"));
        network.put("35.155.49.147:50211", AccountId.fromString("0.0.8"));
        network.put("34.133.197.230:50211", AccountId.fromString("0.0.9"));
        network.put("52.14.252.207:50211", AccountId.fromString("0.0.9"));
        network.put("52.165.17.231:50211", AccountId.fromString("0.0.9"));

        return new Network(executor, network).setLedgerId(LedgerId.TESTNET);
    }

    /**
     * Create a previewnet network.
     *
     * @param executor                  the executor service
     * @return                          the new previewnet network
     */
    static Network forPreviewnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();

        network.put("3.211.248.172:50211", AccountId.fromString("0.0.3"));
        network.put("35.231.208.148:50211", AccountId.fromString("0.0.3"));
        network.put("40.121.64.48:50211", AccountId.fromString("0.0.3"));
        network.put("3.133.213.146:50211", AccountId.fromString("0.0.4"));
        network.put("35.199.15.177:50211", AccountId.fromString("0.0.4"));
        network.put("40.70.11.202:50211", AccountId.fromString("0.0.4"));
        network.put("104.43.248.63:50211", AccountId.fromString("0.0.5"));
        network.put("35.225.201.195:50211", AccountId.fromString("0.0.5"));
        network.put("52.15.105.130:50211", AccountId.fromString("0.0.5"));
        network.put("13.88.22.47:50211", AccountId.fromString("0.0.6"));
        network.put("35.247.109.135:50211", AccountId.fromString("0.0.6"));
        network.put("54.241.38.1:50211", AccountId.fromString("0.0.6"));
        network.put("13.64.170.40:50211", AccountId.fromString("0.0.7"));
        network.put("35.235.65.51:50211", AccountId.fromString("0.0.7"));
        network.put("54.177.51.127:50211", AccountId.fromString("0.0.7"));
        network.put("13.78.232.192:50211", AccountId.fromString("0.0.8"));
        network.put("34.106.247.65:50211", AccountId.fromString("0.0.8"));
        network.put("35.83.89.171:50211", AccountId.fromString("0.0.8"));
        network.put("20.150.136.89:50211", AccountId.fromString("0.0.9"));
        network.put("34.125.23.49:50211", AccountId.fromString("0.0.9"));
        network.put("50.18.17.93:50211", AccountId.fromString("0.0.9"));

        return new Network(executor, network).setLedgerId(LedgerId.PREVIEWNET);
    }
}
