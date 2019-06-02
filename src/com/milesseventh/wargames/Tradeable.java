package com.milesseventh.wargames;

public interface Tradeable {
	public void beginTrade();
	public ResourceStorage getTradeStorage();
	public void endTrade();
	public boolean isCapacityLimited();
	public float getFreeSpace();
}
