package com.milesseventh.wargames.properties;

public class StructureProperties {
	public String title, description;
	public float fightingRange, maxCondition, craftSpeed, buildingPrice;
	
	public StructureProperties(String ntitle, float nfightingRange, float nmaxCondition, float ncraftSpeed, float nbuildingPrice, String ndescription) {
		title = ntitle;
		fightingRange = nfightingRange;
		maxCondition = nmaxCondition;
		craftSpeed = ncraftSpeed;
		buildingPrice = nbuildingPrice;
		description = ndescription;
	}
}
