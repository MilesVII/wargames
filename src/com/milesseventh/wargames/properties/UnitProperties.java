package com.milesseventh.wargames.properties;

public class UnitProperties {
	public String name;
	public float fuelConsumption, minMaxCondition, maxMaxCondition, minSpeed, maxSpeed;
	
	public UnitProperties(String nname, float nfuelConsumption, float nminMaxCondition, float nmaxMaxCondition,float nminSpeed, float nmaxSpeed) {
		name = nname;
		fuelConsumption = nfuelConsumption;
		minMaxCondition = nminMaxCondition;
		maxMaxCondition = nmaxMaxCondition;
		minSpeed = nminSpeed;
		maxSpeed = nmaxSpeed;
	}

}
