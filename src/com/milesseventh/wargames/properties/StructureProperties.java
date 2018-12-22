package com.milesseventh.wargames.properties;

public class StructureProperties {
	public String title;
	public float fightingRange, maxCondition, craftSpeed;
	
	public StructureProperties(String ntitle, float nfightingRange, float nmaxCondition, float ncraftSpeed) {
		title = ntitle;
		fightingRange = nfightingRange;
		maxCondition = nmaxCondition;
		craftSpeed = ncraftSpeed;
	}

}
