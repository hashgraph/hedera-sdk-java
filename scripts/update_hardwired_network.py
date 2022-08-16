#!/usr/bin/env python3

import requests
import os

# make sure the scripts directory is the working directory
os.chdir(os.path.dirname(__file__))

#copied from update_protobufs.py
MAIN_PATH = os.path.join("..", "sdk", "src", "main")
JAVA_OUT_PATH = os.path.join(MAIN_PATH, "java", "com", "hedera", "hashgraph", "sdk")
HARDWIRED_NETWORK_NAME = "HardwiredNetwork"
HARDWIRED_NETWORK_OUT_PATH = os.path.join(JAVA_OUT_PATH, HARDWIRED_NETWORK_NAME + ".java")

NETWORK_NAME = "network"
PORT_IS_DESIRED = lambda port: port == 50211
MAINNET_LEDGER_NAME = "mainnet-public"
TESTNET_LEDGER_NAME = "testnet"
PREVIEWNET_LEDGER_NAME = "previewnet"

def get_network(ledger_name, network_name, port_is_desired):
    return get_network_internal(
        "https://" + ledger_name + ".mirrornode.hedera.com",
        "/api/v1/network/nodes",
        network_name,
        port_is_desired
    )


def get_network_internal(domain, route, network_name, port_is_desired):
    response = requests.get(domain + route)
    response_json = response.json()
    nodes = response_json["nodes"]
    next = response_json["links"]["next"]
    result_lines = []
    for node in nodes:
        account_id = "AccountId.fromString(\"" + node["node_account_id"] + "\")"
        for ep in [ep for ep in node["service_endpoints"] if port_is_desired(ep["port"])]:
            endpoint = "\"" + ep["ip_address_v4"] + ":" + str(ep["port"]) + "\""
            result_lines.append(network_name + ".put(" + endpoint + ", " + account_id + ");")
    if next is None:
        return result_lines
    else:
        return result_lines + get_network_internal(domain, next, network_name, port_is_desired)

def lines_to_string(tab_count, lines):
    return_string = ""
    for line in lines:
        return_string += "    "*tab_count + line + "\n"
    return return_string

#copied from update_protobufs.py
def output_java_file(out_path, section_list):
    out_file = open(out_path, "w")
    for section in section_list:
        out_file.write(section)
    out_file.close()

#copied from update_protobufs.py
def premade(name, n):
    return open(os.path.join("premade", name + "-" + str(n) + ".txt"), "r").read()



mainnet_lines = get_network(MAINNET_LEDGER_NAME, NETWORK_NAME, PORT_IS_DESIRED)
testnet_lines = get_network(TESTNET_LEDGER_NAME, NETWORK_NAME, PORT_IS_DESIRED)
previewnet_lines = get_network(PREVIEWNET_LEDGER_NAME, NETWORK_NAME, PORT_IS_DESIRED)

output_java_file(HARDWIRED_NETWORK_OUT_PATH, [
    premade(HARDWIRED_NETWORK_NAME, 0),
    lines_to_string(2, mainnet_lines),
    premade(HARDWIRED_NETWORK_NAME, 2),
    lines_to_string(2, testnet_lines),
    premade(HARDWIRED_NETWORK_NAME, 4),
    lines_to_string(2, previewnet_lines),
    premade(HARDWIRED_NETWORK_NAME, 6)
])
