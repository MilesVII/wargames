package com.milesseventh.wargames;

public class ResourceStorage {
	public String name;
	private float[] resources = {0, 0, 0, 0, 0};
	
	public ResourceStorage(String nname) {
		name = nname;
	}
	
	public float sum(){
		float r = 0;
		for (float i: resources)
			r += i;
		return r;
	}

	public void add(Resource r, float size){
		resources[r.ordinal()] += size;
	}
	
	public float get(Resource r){
		return resources[r.ordinal()];
	}
	
	public boolean isEnough(Resource r, float size){
		return resources[r.ordinal()] >= size;
	}

	public boolean tryRemove(Resource r, float size){
		if (isEnough(r, size)){
			resources[r.ordinal()] -= size;
			return true;
		} else
			return false;
	}
	
	public boolean tryTransfer(Resource r, float size, ResourceStorage rs){
		if (tryRemove(r, size)){
			rs.add(r, size);
			return true;
		} else
			return false;
	}

	public void flushTo(Resource r, ResourceStorage rs){
		assert(tryTransfer(r, get(r), rs));
	}
	
	public void fullFlushTo(ResourceStorage rs){
		for (Resource r: Resource.values())
			assert(tryTransfer(r, get(r), rs));
	}
	
	public void set(Resource r, float size){
		resources[r.ordinal()] = size;
	}
}
