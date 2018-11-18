package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.Heartstrings.SpecialTechnology;

public class YardDialog {
	public ArrayList<Unit> selectedUnitsForDeployment = new ArrayList<Unit>();
	public SpecialTechnology[] availableST;
	public ArrayList<SpecialTechnology> stToAdd = new ArrayList<SpecialTechnology>();
	public Unit lastChecked = null;
	
	public YardDialog() {}
	
	public void select(Unit u){
		lastChecked = u;
		
		if (selectedUnitsForDeployment.contains(u))
			selectedUnitsForDeployment.remove(u);
		else if (!u.isRepairing)
			selectedUnitsForDeployment.add(u);
		
		availableST = Heartstrings.get(Heartstrings.fromUnitType(u.type), Heartstrings.craftableProperties).availableSTs;
		stToAdd.clear();
	}
	
	public void checkST(SpecialTechnology st){
		if (stToAdd.contains(st))
			stToAdd.remove(st);
		else
			stToAdd.add(st);
	}
	
	public void reset(){
		selectedUnitsForDeployment.clear();
		lastChecked = null;
	}
}
