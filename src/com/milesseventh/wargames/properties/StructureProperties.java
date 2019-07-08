package com.milesseventh.wargames.properties;

public class StructureProperties {
	public String title, description;
	public float fightingRange, maxCondition, craftSpeed;
	public int buildingPrice;
	
	public StructureProperties(String ntitle, float nfightingRange, float nmaxCondition, float ncraftSpeed, int nbuildingPrice, String ndescription) {
		title = ntitle;
		fightingRange = nfightingRange;
		maxCondition = nmaxCondition;
		craftSpeed = ncraftSpeed;
		buildingPrice = nbuildingPrice;
		description = ndescription;
	}
}
