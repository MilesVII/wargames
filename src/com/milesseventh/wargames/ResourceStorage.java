package com.milesseventh.wargames;

public class ResourceStorage {
	public String name;
	private int[] resources = {0, 0, 0, 0, 0};
	
	public ResourceStorage(String nname) {
		name = nname;
	}
	
	public int sum(){
		int r = 0;
		for (int i: resources)
			r += i;
		return r;
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

	public void flushTo(Resource r, ResourceStorage rs){
		boolean x = tryTransfer(r, get(r), rs);
		assert(x);
	}
	
	public void fullFlushTo(ResourceStorage rs){
		for (Resource r: Resource.values()){
			boolean x = tryTransfer(r, get(r), rs);
			assert(x);
		}
	}
	
	public void set(Resource r, int size){
		resources[r.ordinal()] = size;
	}
}
