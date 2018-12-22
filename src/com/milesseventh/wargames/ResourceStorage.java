package com.milesseventh.wargames;

public class ResourceStorage {
	public String name;
	private int[] resources = {0, 0, 0, 0, 0, 0};
	
	public ResourceStorage(String nname) {
		name = nname;
	}

	public void add(Resource r, int size){
		resources[r.ordinal()] += size;
	}
	
	public int get(Resource r){
		return resources[r.ordinal()];
	}
	
	public boolean isEnough(Resource r, int size){
		return resources[r.ordinal()] >= size;
	}

	public boolean tryRemove(Resource r, int size){
		if (isEnough(r, size)){
			resources[r.ordinal()] -= size;
			return true;
		} else
			return false;
	}
	
	public boolean tryTransfer(Resource r, int size, ResourceStorage rs){
		if (tryRemove(r, size)){
			rs.add(r, size);
			return true;
		} else
			return false;
	}
}
