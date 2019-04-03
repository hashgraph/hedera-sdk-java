package com.hedera.sdk.common;

public class HederaExchangeRate {
   	private int hbarEquiv = 0;
   	private int centEquiv = 0;
   	private long expirationTime = 0;

   	public HederaExchangeRate(int hbarEquivalent, int centEquivalent, long expirationTime) {
		this.hbarEquiv = hbarEquivalent;
		this.centEquiv = centEquivalent;
   		this.expirationTime = expirationTime;
   	}
   	
   	public int getHbarEquivalent() {
   		return this.hbarEquiv;
   	}
   	public int getCentEquivalent() {
   		return this.centEquiv;
   	}
   	public long getExpirationTime() {
   		return this.expirationTime;
   	}
}
