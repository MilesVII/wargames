package com.milesseventh.wargames;

public class ResourceStorage {
	public String name;
	//AMMO, OIL, FUEL
	private float[] fluids = {0, 0, 0};
	//ORE, METAL, MISSILE
	private int[] pieces = {0, 0, 0};
	
	public ResourceStorage(String nname) {
		name = nname;
	}

	public void add(Resource r, float size){
		if (r.ordinal() < 3) //fluid
			fluids[r.ordinal()] += size;
		else                 //piece
			pieces[r.ordinal() - 3] += Math.round(size);
	}
	
	public int get(Resource r){
		if (r.ordinal() < 3) //fluid
			return (int)Math.floor(fluids[r.ordinal()]);
		else                 //piece
			return pieces[r.ordinal() - 3];
		//return resources[r.ordinal()];
	}
	
	public float getFluid(Resource r){
		return fluids[r.ordinal()];
	}
	
	public float getPieces(Resource r){
		return pieces[r.ordinal() - 3];
	}
	
	public boolean isEnough(Resource r, float size){
		if (r.ordinal() < 3)
			return fluids[r.ordinal()] >= size;
		else
			return pieces[r.ordinal() - 3] >= Math.floor(size);
	}

	public boolean tryRemove(Resource r, float size){
		if (isEnough(r, size)){
			if (r.ordinal() < 3)
				fluids[r.ordinal()] -= size;
			else
				pieces[r.ordinal() - 3] -= Math.floor(size);
			
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
	
	public void set(Resource r, int size){
		if (r.ordinal() < 3)
			fluids[r.ordinal()] = size;
		else
			pieces[r.ordinal() - 3] = size;
	}
}
