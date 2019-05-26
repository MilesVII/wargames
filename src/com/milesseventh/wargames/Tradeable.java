package com.milesseventh.wargames;

public interface Tradeable {
	public String getName();
	public void prepareToTrade();
	public ResourceStorage getTradeStorage();
	public void doneTrading();
	public boolean isCapacityLimited();
	public float getFreeSpace();
}
