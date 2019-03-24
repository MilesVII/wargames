package com.milesseventh.wargames.properties;

import com.milesseventh.wargames.Heartstrings;

public class UnitProperties {
	public String name;
	public float fuelConsumption, minMaxCondition, maxMaxCondition, minSpeed, maxSpeed;
	private int receiveFireChance;
	
	public UnitProperties(String nname, float nfuelConsumption, float nminMaxCondition, float nmaxMaxCondition,float nminSpeed, float nmaxSpeed, int receiveFireChance) {
		name = nname;
		fuelConsumption = nfuelConsumption;
		minMaxCondition = nminMaxCondition;
		maxMaxCondition = nmaxMaxCondition;
		minSpeed = nminSpeed;
		maxSpeed = nmaxSpeed;
		receiveFireChance = nreceiveFireChance;
	}

	public float getReceiveFireChance(){
		float s = 0;
		for (UnitProperties up: Heartstrings.uProperties){
			s += up.receiveFireChance;
		}
		return receiveFireChance / s;
	}
}
