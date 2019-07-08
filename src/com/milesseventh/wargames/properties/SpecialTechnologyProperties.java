package com.milesseventh.wargames.properties;

import com.milesseventh.wargames.Faction;
import com.milesseventh.wargames.Heartstrings;
import com.milesseventh.wargames.Utils;
import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class SpecialTechnologyProperties {
	public static class TechnologyRequirement{
		public Technology tech;
		public float level;
		
		public TechnologyRequirement(Technology t, float minLevel){
			tech = t;
			level = minLevel;
		}
	}
	
	public int investigationWorkamount, workamountMarkup, investigationPriceInData, priceMarkupInMetal;
	public String title, description;
	public TechnologyRequirement[] techReqs;
	public SpecialTechnology[] stReqs;
	public String techReqsDescription;
	public SpecialTechnologyProperties(String tit, String desc,
	                                   int invWork, int workMarkup, 
	                                   int invPrice, int priceMarkup,
	                                   TechnologyRequirement[] tr,
	                                   SpecialTechnology[] str) {
		title                    = tit;
		description              = Utils.splitIntoLines(desc, 32);
		investigationWorkamount  = invWork;
		workamountMarkup         = workMarkup;
		investigationPriceInData = invPrice;
		priceMarkupInMetal       = priceMarkup;
		techReqs                 = tr;
		stReqs                   = str;
		
		techReqsDescription = "Requirements:\n";
		for (TechnologyRequirement req: techReqs)
			techReqsDescription += Heartstrings.get(req.tech, Heartstrings.tProperties).shortTitle + 
			                       ": " + String.format("%.2f%%", req.level * 100f) + "\n";
		techReqsDescription += "Science: " + Math.round(investigationPriceInData);
		
		//description = descriptionAppendix + description;
	}
	
	public boolean areBasicSTInvestigated(Faction f){
		for (SpecialTechnology st: stReqs)
			if (!f.isInvestigated(st))
				return false;
		return true;
	}
}
