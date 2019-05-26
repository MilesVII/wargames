package com.milesseventh.wargames.dialogs;

import com.milesseventh.wargames.Resource;
import com.milesseventh.wargames.Squad;
import com.milesseventh.wargames.Tradeable;
import com.milesseventh.wargames.Unit;

public class TradeDialog {
	public Resource selectedResource = null;
	
	public TradeDialog() {
		// TODO Auto-generated constructor stub
	}

	public float getTradeableResourceShare(Tradeable A, Tradeable B){
		//0: Amount of shareable resources is limited by overall amount of resources
		float result = A.getTradeStorage().get(selectedResource) + B.getTradeStorage().get(selectedResource);
		//1 and 2: Capacity of each side as another constraint
		if (A.isCapacityLimited())
			result = Math.min(result, A.getFreeSpace() + A.getTradeStorage().get(selectedResource));
		if (B.isCapacityLimited())
			result = Math.min(result, B.getFreeSpace() + B.getTradeStorage().get(selectedResource));
		
		return result;
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
