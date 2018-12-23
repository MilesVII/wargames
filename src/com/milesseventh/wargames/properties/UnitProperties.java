package com.milesseventh.wargames.properties;

public class UnitProperties {
	public String name;
	public float fuelConsumption, minMaxCondition, maxMaxCondition;
	
	public UnitProperties(String nname, float nfuelConsumption, float nminMaxCondition, float nmaxMaxCondition) {
		name = nname;
		fuelConsumption = nfuelConsumption;
		minMaxCondition = nminMaxCondition;
		maxMaxCondition = nmaxMaxCondition;
	}

}
