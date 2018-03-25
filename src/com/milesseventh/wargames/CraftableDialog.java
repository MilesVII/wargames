package com.milesseventh.wargames;

import java.util.ArrayList;

import com.milesseventh.wargames.Heartstrings.Craftable;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class CraftableDialog {
	public Fraction fraction;
	public Craftable selected;
	public ArrayList<SpecialTechnology> selectedST = new ArrayList<SpecialTechnology>();
	public String[] availableSTTitles;
	public SpecialTechnology[] availableST;
	public float selectedT[] = new float[Technology.values().length];
	
	public CraftableDialog(){
		//fraction = Fraction.debug;//TODO
		select(Craftable.TRANSPORTER);
	}
	
	public void select(Craftable c){
		selected = c;
		generateAvailableSTBySelected();
		selectedST.clear();
		for (int i = 0; i < selectedT.length; i++)
			selectedT[i] = 0;
	}
	
	public boolean isSTSelected(SpecialTechnology st){
		return selectedST.contains(st);
	}
	
	public void toggleST(SpecialTechnology st){
		if (isSTSelected(st))
			selectedST.remove(st);
		else
			selectedST.add(st);
	}
	
	private void generateAvailableSTBySelected(){
		fraction = Fraction.debug;//TODO
		int len = 0, j = 0;
		for (int i = 0; i < Heartstrings.get(selected, Heartstrings.craftableProperties).availableSTs.length; i++)
			if (fraction.isInvestigated(Heartstrings.get(selected, Heartstrings.craftableProperties).availableSTs[i]))
				len++;
		availableST = new SpecialTechnology[len];
		for (int i = 0; i < Heartstrings.get(selected, Heartstrings.craftableProperties).availableSTs.length; i++)
			if (fraction.isInvestigated(Heartstrings.get(selected, Heartstrings.craftableProperties).availableSTs[i])){
				availableST[j] = Heartstrings.get(selected, Heartstrings.craftableProperties).availableSTs[i];
				j++;
			}
		availableSTTitles = new String[availableST.length];
		for (int i = 0; i < availableST.length; i++)
			availableSTTitles[i] = Heartstrings.get(availableST[i], Heartstrings.stProperties).title;
	}
}
