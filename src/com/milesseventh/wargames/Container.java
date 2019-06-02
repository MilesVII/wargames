package com.milesseventh.wargames;

import com.badlogic.gdx.math.Vector2;

public class Container implements Tradeable {
	private ResourceStorage resources = new ResourceStorage("Debris");
	public Vector2 position;
	
	public Container(Vector2 nposition) {
		position = nposition;
		Faction.containers.add(this);
	}

	@Override
	public void beginTrade() {}

	@Override
	public ResourceStorage getTradeStorage() {
		return resources;
	}

	@Override
	public void endTrade() {
//		if (resources.sum() < 10)
//			Faction.containers.remove(this);
	}

	@Override
	public boolean isCapacityLimited() {
		return false;
	}

	@Override
	public float getFreeSpace() {
		assert(false);
		return Float.POSITIVE_INFINITY;
	}

}
