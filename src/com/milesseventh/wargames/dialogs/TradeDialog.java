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
		assert(selectedResource != Resource.MISSILE); //TODO
		System.out.println(s.getCapacity() - (s.resources.sum() - s.resources.get(Resource.MISSILE) - s.resources.get(selectedResource)));
		return Math.min(s.tradePartner.get(selectedResource), 
		                s.getCapacity() - (s.resources.sum() - s.resources.get(Resource.MISSILE) - s.resources.get(selectedResource)));
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
	
	private static Comparator<Unit> sortByCapacity = new Comparator<Unit>(){
		@Override
		public int compare(Unit u0, Unit u1) {
			float cargo0 = u0.type == Unit.Type.TRANSPORTER ? u0.getCapacity() : Float.POSITIVE_INFINITY;
			float cargo1 = u1.type == Unit.Type.TRANSPORTER ? u1.getCapacity() : Float.POSITIVE_INFINITY;
			
			return (int)Math.signum(cargo0 - cargo1);
		}
	};
	
	public static Unit getTransporterByID(Squad s, int id){
		s.units.sort(sortByCapacity);
		return s.units.get(id);
	}
}
