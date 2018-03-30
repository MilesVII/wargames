package com.milesseventh.wargames;

import com.milesseventh.wargames.Heartstrings.SpecialTechnology;
import com.milesseventh.wargames.Heartstrings.Technology;
import com.milesseventh.wargames.Structure.Resource;

public class CraftableProperties {
	public String title, description;
	public Resource[] ingridients;
	public Technology[] availableTechs;
	public SpecialTechnology[] availableSTs;
	public float[] relativeCosts;
	public float relativeWorkamount;
	
	public CraftableProperties(String _title, String _description, 
	                           Resource[] _ingridients, float[] _relativeCosts, 
	                           Technology[] _availableTechs,
	                           SpecialTechnology[] _availableSTs, 
	                           float _relativeWorkamount) {
		assert(_ingridients.length == _relativeCosts.length);
		title              = _title;
		description        = _description;
		ingridients        = _ingridients;
		relativeCosts      = _relativeCosts;
		availableTechs     = _availableTechs;
		availableSTs       = _availableSTs;
		relativeWorkamount = _relativeWorkamount;
	}
	
	public float getSingleCraftingCost(Resource res){
		for (int i = 0; i < ingridients.length; i++){
			if (ingridients[i] == res)
				return relativeCosts[i];
		}
		return 0;
	}
}
