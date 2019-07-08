package com.milesseventh.wargames.properties;

import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;
import com.milesseventh.wargames.Resource;

public class CraftableProperties {
	public String title, description;
	public Resource[] ingridients;
	public Technology[] availableTechs;
	public SpecialTechnology[] availableSTs;
	public int[] relativeCosts;
	public int workamount;
	
	public CraftableProperties(String ntitle, String ndescription, 
	                           Resource[] ningridients, int[] nrelativeCosts, 
	                           Technology[] navailableTechs,
	                           SpecialTechnology[] navailableSTs, 
	                           int nworkamount) {
		assert(ningridients.length == nrelativeCosts.length);
		title              = ntitle;
		description        = ndescription;
		ingridients        = ningridients;
		relativeCosts      = nrelativeCosts;
		availableTechs     = navailableTechs;
		availableSTs       = navailableSTs;
		workamount = nworkamount;
	}
	
	public int getSingleCraftingCost(Resource res){
		for (int i = 0; i < ingridients.length; i++){
			if (ingridients[i] == res)
				return relativeCosts[i];
		}
		return 0;
	}
}
