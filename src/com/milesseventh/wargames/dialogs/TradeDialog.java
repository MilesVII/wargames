package com.milesseventh.wargames.dialogs;

import com.badlogic.gdx.math.Vector2;
import com.milesseventh.wargames.Resource;
import com.milesseventh.wargames.ResourceStorage;
import com.milesseventh.wargames.Squad;
import com.milesseventh.wargames.Tradeable;
import com.milesseventh.wargames.Unit;

public class TradeDialog {
	public Resource selectedResource = null;
	public ResourceStorage dispenser = new ResourceStorage("");
	
	public TradeDialog() {
		// TODO Auto-generated constructor stub
	}

	public Vector2 allToA = new Vector2(); //x == A, y == B
	public Vector2 allToB = new Vector2();
	public int resourceSharingRange = 0;
	public int estimateTradeableResourceShare(Tradeable A, Tradeable B){
		float fullResources = A.getTradeStorage().get(selectedResource) + B.getTradeStorage().get(selectedResource);
		
		//check all-to-A case
		if (A.isCapacityLimited()){
			float operableCapacity = A.getFreeSpace() + A.getTradeStorage().get(selectedResource);
			allToA.x = Math.min(fullResources, operableCapacity);
			allToA.y = Math.max(fullResources - operableCapacity, 0);
		} else {
			allToA.x = fullResources;
			allToA.y = 0;
		}
		//check all-to-B case
		if (B.isCapacityLimited()){
			float operableCapacity = B.getFreeSpace() + B.getTradeStorage().get(selectedResource);
			allToB.y = Math.min(fullResources, operableCapacity);
			allToB.x = Math.max(fullResources - operableCapacity, 0);
		} else {
			allToB.y = fullResources;
			allToB.x = 0;
		}
		
		resourceSharingRange = (int)Math.floor(Math.abs(allToA.x - allToA.y));
		return resourceSharingRange;
	}
	
	public void reset(){
		selectedResource = null;
	}
	
	public static int countTransporters(Squad s){
		int i = 0;
		for (Unit u: s.units)
			if (u.type == Unit.Type.TRANSPORTER)
				++i;
		return i;
	}
	
	public static Unit getTransporterByID(Squad s, int id){
		s.units.sort(Squad.sortByCapacity);
		return s.units.get(id);
	}
}
