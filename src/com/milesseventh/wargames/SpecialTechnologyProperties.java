package com.milesseventh.wargames;

import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;

public class SpecialTechnologyProperties {
	public static class TechnologyRequirement{
		Technology tech;
		float level;
		
		public TechnologyRequirement(Technology t, float minLevel){
			tech = t;
			minLevel = level;
		}
	}
	
	public float investigationWorkamount, workamountMarkup, investigationPriceInData, priceMarkupInMetal;
	public String title, description;
	public TechnologyRequirement[] techReqs;
	public SpecialTechnology[] stReqs;
	
	public SpecialTechnologyProperties(String tit, String desc,
	                                   float invWork, float workMarkup, 
	                                   float invPrice, float priceMarkup,
	                                   TechnologyRequirement[] tr,
	                                   SpecialTechnology[] str) {
		title                         = tit;
		description                   = desc;
		investigationWorkamount       = invWork;
		workamountMarkup              = workMarkup;
		investigationPriceInData      = invPrice;
		priceMarkupInMetal            = priceMarkup;
		techReqs                      = tr;
		stReqs                        = str;
		
		/*String descriptionAppendix = "Requirements:\n";
		for (SpecialTechnology req: stReqs)
			descriptionAppendix += Heartstrings.get(req, Heartstrings.stProperties).title + "\n";
		for (TechnologyRequirement req: techReqs)
			descriptionAppendix += Heartstrings.get(req.tech, Heartstrings.technologyShortTitles) + 
			                       ": " + String.format("%.2f%%", req.level * 100f) + "\n";*/
		
		//description = descriptionAppendix + description;
	}
	
	public boolean isInvestigationAllowed(Fraction f){
		for (TechnologyRequirement tr: techReqs)
			if (f.techLevel(tr.tech) < tr.level)
				return false;
		for (SpecialTechnology st: stReqs)
			if (!f.isInvestigated(st))
				return false;
		return true;
	};
}
