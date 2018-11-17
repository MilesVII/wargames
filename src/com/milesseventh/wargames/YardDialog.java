package com.milesseventh.wargames;

import java.util.ArrayList;

public class YardDialog {
	public ArrayList<Unit> selectedUnitsForDeployment = new ArrayList<Unit>();
	public Unit lastChecked = null;
	
	public YardDialog() {
		// TODO Auto-generated constructor stub
	}
	
	public void reset(){
		selectedUnitsForDeployment.clear();
		lastChecked = null;
	}
}
