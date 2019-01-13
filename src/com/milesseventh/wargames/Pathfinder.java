package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public class Pathfinder {
	@SuppressWarnings("serial")
	public static class Node extends Vector2{
		public float value;
		public int generation;
		public Node parent;
		public Node (Vector2 position, float nvalue, Node nparent){
			super(position);
			value = nvalue;
			parent = nparent;
			generation = (parent == null ? 1 : parent.generation + 1);
		}
	}
	public interface Stridable{
		public boolean isWalkable(float x, float y);
		public Vector2 getSize();
	}
	
	private static final Vector2[] SPF_DIRECTIONS= { //Seventh PathFinder
		new Vector2( 1,  0),
		new Vector2( 1,  1),
		new Vector2( 0,  1),
		new Vector2(-1,  1),
		new Vector2(-1,  0),
		new Vector2(-1, -1),
		new Vector2( 0, -1),
		new Vector2( 1, -1)
	};
	public static float findWalkableArea(Stridable map, float step, Vector2 from){
		from = from.cpy();
		
		//Prepare direction offset vectors
		Vector2[] directionOffsets = new Vector2[SPF_DIRECTIONS.length];
		for (int i = 0; i < SPF_DIRECTIONS.length; ++i)
			directionOffsets[i] = SPF_DIRECTIONS[i].cpy().scl(step);
		
		//Align to path grid
		from.set(Math.round(from.x / step) * step, Math.round(from.y / step) * step);
		
		//Avoid fullscan and noscan
		if (safetyCheck(from, map.getSize()) && 
		    !map.isWalkable(from.x, from.y))
			return 0;
		
		ArrayList<Node> shockwave = new ArrayList<Node>();
		ArrayList<Node> dust = new ArrayList<Node>();
		shockwave.add(new Node(from, 0, null));
		
		while(!shockwave.isEmpty()){
			Node pivot = null; //Finding node with minimal value
			for (Node hook: shockwave)
				if (pivot == null || hook.value < pivot.value)
					pivot = hook;

			//Explode the pivot
			for (Vector2 offset: directionOffsets){
				Vector2 debris = pivot.cpy().add(offset);
				if (map.isWalkable(debris.x, debris.y) && 
				    safetyCheck(debris, map.getSize()) && 
				    !isDust(shockwave, debris, step / 2f) &&
				    !isDust(dust, debris, step / 2f))
					shockwave.add(new Node(debris, pivot.value + offset.len2(), pivot));
			}

			//Remove pivot from shockwave and check for distance
			shockwave.remove(pivot);
			dust.add(pivot);
		}
		
		return dust.size();
	}
	public static Node findPath(Stridable map, float step, Vector2 from, Vector2 to){
		from = from.cpy();
		  to =   to.cpy();
		
		//Prepare direction offset vectors
		Vector2[] directionOffsets = new Vector2[SPF_DIRECTIONS.length];
		for (int i = 0; i < SPF_DIRECTIONS.length; ++i)
			directionOffsets[i] = SPF_DIRECTIONS[i].cpy().scl(step);
		
		//Align to path grid
		from.set(Math.round(from.x / step) * step, Math.round(from.y / step) * step);
		  to.set(Math.round  (to.x / step) * step, Math.round  (to.y / step) * step);
		
		//Avoid fullscan and noscan
		if (safetyCheck(from, map.getSize()) && 
		    safetyCheck(  to, map.getSize()) && 
		    (!map.isWalkable(from.x, from.y) || !map.isWalkable(to.x, to.y)))
			return null;
		if (isSameNode(to, from, step / 2f))
			return new Node(from, 0, null);
		
		ArrayList<Node> shockwave = new ArrayList<Node>();
		ArrayList<Node> dust = new ArrayList<Node>();
		shockwave.add(new Node(from, 0, null));
		
		Node bestResult = null;
		while(!shockwave.isEmpty()){
			Node pivot = null; //Finding node with minimal value
			for (Node hook: shockwave)
				if (pivot == null || hook.value < pivot.value)
					pivot = hook;

			//Explode the pivot
			for (Vector2 offset: directionOffsets){
				Vector2 debris = pivot.cpy().add(offset);
				if (map.isWalkable(debris.x, debris.y) && 
				    safetyCheck(debris, map.getSize()) && 
				    !isDust(shockwave, debris, step / 2f) &&
				    !isDust(dust, debris, step / 2f))
					shockwave.add(new Node(debris, pivot.value + offset.len2(), pivot));
			}

			//Remove pivot from shockwave and check for distance
			shockwave.remove(pivot);
			if (isSameNode(pivot, to, step / 2f) && (bestResult == null || pivot.value < bestResult.value))
				bestResult = pivot;
			else
				dust.add(pivot);
		}
		
		return bestResult;
	}
	
	public static Vector2[] convertNodeToPath(Node x){
		if (x == null)
			return null;
		
		Vector2[] r = new Vector2[x.generation];
		for (int i = r.length - 1; i >= 0; --i){
			assert(x != null);
			r[i] = x;
			x = x.parent;
		}
		
		return r;
	}
	
	private static boolean safetyCheck(Vector2 ghostNode, Vector2 size){
		return (ghostNode.x < size.x && ghostNode.x > 0 &&
		        ghostNode.y < size.y && ghostNode.y > 0);
	}
	
	private static boolean isDust(ArrayList<Node> dust, Vector2 debris, float epsilon){
		for (Node echo: dust)
			if (isSameNode(echo, debris, epsilon))
				return true;
		return false;
	}
	private static boolean isSameNode(Vector2 a, Vector2 b, float epsilon){
		return a.dst2(b) < epsilon * epsilon;
	}
	/* Old implementation of partially-implemented greedy-first search used to check if two points are accessible to each other
	public Pathfinder (Stridable map, float nstep){
		step = nstep;
		nodemap = new Node[(int)map.getSize().x][(int)map.getSize().y];
		collisionmap = new boolean[(int)map.getSize().x][(int)map.getSize().y];
		for (int x = 0; x < map.getSize().x; x += step)
			for (int y = 0; y < map.getSize().y; y += step){
				nodemap[(int)(x / step)][(int)(y / step)] = new Node(x / step, y / step);
				collisionmap[(int)(x / step)][(int)(y / step)] = map.isWalkable(x, y);
			}
	}
	
	public boolean isAccessible(Vector2 from, Vector2 to){
		ArrayList<Node> frontwave = new ArrayList<Node>();
		Node horsey;
		Node[][] nm = nodemap.clone();
		
		//Align to path grid
		from.set(Math.round(from.x / step), Math.round(from.y / step));
		to.set(Math.round(to.x / step), Math.round(to.y / step));
		
		//Avoid fullscan
		if (!collisionmap[(int)from.x][(int)from.y] || !collisionmap[(int)to.x][(int)to.y])
			return false;
		
		frontwave.add(nm[(int)from.x][(int)from.y]);
		while (!frontwave.isEmpty()){
			horsey = findBest(frontwave, to);

			if (horsey.x == to.x && horsey.y == to.y)
				return true;
			
			//Let horsey grow!
			if (horsey.x - 1 >= 0)
				letHorseyGrow(frontwave, nm, (int)horsey.x - 1, (int)horsey.y);
			if (horsey.x + 1 < nm.length)
				letHorseyGrow(frontwave, nm, (int)horsey.x + 1, (int)horsey.y);
			if (horsey.y - 1 >= 0)
				letHorseyGrow(frontwave, nm, (int)horsey.x, (int)horsey.y - 1);
			if (horsey.y + 1 < nm[0].length)
				letHorseyGrow(frontwave, nm, (int)horsey.x, (int)horsey.y + 1);
			
			horsey.isChecked = true;
			frontwave.remove(horsey);
		}
		return false;
	}
	
	private void letHorseyGrow(ArrayList<Node> fw, Node[][] nm, int x, int y){
		if (collisionmap[x][y]){
			if (!nm[x][y].isChecked){
				nm[x][y].isChecked = true;
				fw.add(nm[x][y]);
			}
		}
	}
	
	private Node findBest(ArrayList<Node> list, Vector2 to){
		Node best = list.get(0);
		for (Node runner: list)
			if (manhattan(runner, to) < manhattan(best, to))
				best = runner;
		return best;
	}
	
	private float manhattan(Vector2 a, Vector2 b){
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}*/
}
