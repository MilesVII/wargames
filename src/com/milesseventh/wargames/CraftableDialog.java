package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;

public class CraftableDialog {
	public Fraction fraction;
	public Craftable selected;
	public ArrayList<SpecialTechnology> selectedST = new ArrayList<SpecialTechnology>();
	public String[] availableSTTitles;
	public SpecialTechnology[] availableST;
	
	public CraftableDialog(Fraction f) {
		fraction = f;
		select(Craftable.TRANSPORTER);
	}
	
	public void select(Craftable c){
		selected = c;
		generateAvailableSTBySelected();
	}
	
	public void generateAvailableSTBySelected(){
		int len = 0, j = 0;
		for (int i = 0; i < Heartstrings.get(selected, Heartstrings.availableCraftablesST).length; i++)
			if (fraction.isInvestigated(Heartstrings.get(selected, Heartstrings.availableCraftablesST)[i]))
				len++;
		availableST = new SpecialTechnology[len];
		for (int i = 0; i < Heartstrings.get(selected, Heartstrings.availableCraftablesST).length; i++)
			if (fraction.isInvestigated(Heartstrings.get(selected, Heartstrings.availableCraftablesST)[i])){
				availableST[j] = Heartstrings.get(selected, Heartstrings.availableCraftablesST)[i];
				j++;
			}
		availableSTTitles = new String[availableST.length];
		for (int i = 0; i < availableST.length; i++)
			availableSTTitles[i] = Heartstrings.get(availableST[i], Heartstrings.specialTechnologyTitles);
	}
}
