package com.milesseventh.wargames.dialogs;

import java.util.ArrayList;

import com.milesseventh.wargames.Squad;
import com.milesseventh.wargames.Unit;

public class MissileExchangeDialog {
	public MissileExchangeDialog() {}

	public ArrayList<Squad> nearby = new ArrayList<Squad>();
	private ArrayList<Squad> temporaryNearby = new ArrayList<Squad>();
	public ArrayList<Squad> filterNearby(){
		ArrayList<Squad> swaper;
		
		temporaryNearby.clear();
		for (Squad s: nearby){
			if (s.isUnitTypePresent(Unit.Type.TRANSPORTER) &&
				s.state == Squad.State.STAND)
				temporaryNearby.add(s);
		}
		
		swaper = temporaryNearby;
		temporaryNearby = nearby;
		nearby = swaper;
		
		return nearby;
	}
}
