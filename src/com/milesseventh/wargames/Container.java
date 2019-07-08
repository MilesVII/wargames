package com.milesseventh.wargames;

import com.badlogic.gdx.math.Vector2;

public class Container implements Tradeable {
	private ResourceStorage resources = new ResourceStorage("Debris");
	public Vector2 position;
	public float lifetimeInSeconds = 60;
	
	public Container(Vector2 nposition, ResourceStorage rs) {
		position = nposition;
		
		Structure s = Utils.findNearestStructure(null, position, null);
		if (s != null && s.position.dst2(position) < Heartstrings.INTERACTION_DISTANCE2)
			rs.fullFlushTo(s.resources);
		else {
			rs.fullFlushTo(resources);
			Faction.containers.add(this);
		}
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
