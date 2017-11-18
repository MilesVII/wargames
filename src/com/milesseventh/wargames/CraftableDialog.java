package com.milesseventh.wargames;

import java.util.ArrayList;

public class CraftableDialog {
	public Fraction fraction;
	public Fraction.Craftable selected;
	public ArrayList<Fraction.SpecialTechnology> selectedST = new ArrayList<Fraction.SpecialTechnology>();
	public String[] availableSTTitles;
	public Fraction.SpecialTechnology[] availableST;
	
	public CraftableDialog(Fraction f) {
		fraction = f;
		select(Fraction.Craftable.TRANSPORTER);
	}
	
	public void select(Fraction.Craftable c){
		selected = c;
		generateAvailableSTBySelected();
	}
	
	public void generateAvailableSTBySelected(){
		int len = 0, j = 0;

		for (int i = 0; i < fraction.availableCraftablesST[selected.ordinal()].length; i++)
			if (fraction.isInvestigated(fraction.availableCraftablesST[selected.ordinal()][i]))
				len++;
		availableST = new Fraction.SpecialTechnology[len];
		for (int i = 0; i < fraction.availableCraftablesST[selected.ordinal()].length; i++)
			if (fraction.isInvestigated(fraction.availableCraftablesST[selected.ordinal()][i])){
				availableST[j] = fraction.availableCraftablesST[selected.ordinal()][i];
				j++;
			}
		availableSTTitles = new String[availableST.length];
		for (int i = 0; i < availableST.length; i++)
			availableSTTitles[i] = Fraction.specialTechnologyTitles[availableST[i].ordinal()];
	}
}
