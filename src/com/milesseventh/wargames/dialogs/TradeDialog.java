package com.milesseventh.wargames.dialogs;

import java.util.Comparator;

import com.milesseventh.wargames.Resource;
import com.milesseventh.wargames.Squad;
import com.milesseventh.wargames.Unit;

public class TradeDialog {
	public Resource selectedResource = null;
	
	public TradeDialog() {
		// TODO Auto-generated constructor stub
	}
	
	public float getMaxLoad(Squad s){
		return Math.min(s.tradePartner.get(selectedResource) + s.resources.get(selectedResource), 
		                s.getFreeSpace(true) + s.resources.get(selectedResource));
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
