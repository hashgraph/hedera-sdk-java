package com.hedera.sdk.node;

import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.node.HederaNode;

public class HederaNodeList {
	private final static Random random = new Random();
	private static HederaNode[] nodes;
	
	public HederaNodeList() {
		
	}
	public HederaNodeList(JSONArray nodeList) {
		HederaNodeList.nodes = new HederaNode[nodeList.size()];
		
		for (int i=0; i < nodeList.size(); i++) {
			JSONObject nodeDetails = (JSONObject) nodeList.get(i);
			String host = (String) nodeDetails.get("host");
			int port = (int)((long) nodeDetails.get("port"));
			String account = (String) nodeDetails.get("account");
			HederaAccountID accountId = new HederaAccountID(account);
			
			HederaNode node = new HederaNode(host, port, accountId);
			HederaNodeList.nodes[i] = node;
		}
	}

	public static HederaNode randomNode() {
		if (HederaNodeList.nodes.length == 0) {
			return null;
		}
		int next = random.nextInt(HederaNodeList.nodes.length);
		System.out.println("Selected node " + HederaNodeList.nodes[next].getHost() + "/" + HederaNodeList.nodes[next].getAccountID().toString());
		return HederaNodeList.nodes[next];
	}
	
	public static HederaNode byAccountNum(long accountNum) {
		for (int i=0; i < HederaNodeList.nodes.length; i++) {
			if (HederaNodeList.nodes[i].getAccountID().accountNum == accountNum) {
				return HederaNodeList.nodes[i];
			}
		}
		return null;
	}
	public static HederaNode byHost(String host) {
		for (int i=0; i < HederaNodeList.nodes.length; i++) {
			if (HederaNodeList.nodes[i].getHost() == host) {
				return HederaNodeList.nodes[i];
			}
		}
		return null;
	}
}
