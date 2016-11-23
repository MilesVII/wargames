package com.milesseventh.wargames;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public class Pathfinder {
	public class Node extends Vector2{
		//float x, y;
		boolean isChecked = false;
		public Node (float _x, float _y){super(_x, _y);}
		public void check(){isChecked = true;}
		public void uncheck(){isChecked = false;}
		public boolean isChecked(){return isChecked;}
	}
	public interface Stridable{
		public boolean isWalkable(float _x, float _y);
		public Vector2 getSize();
	}

	private Node[][] nodemap;
	private boolean[][] collisionmap;
	private float step;
	
	public Pathfinder (Stridable _map, float _step){
		step = _step;
		nodemap = new Node[(int)_map.getSize().x][(int)_map.getSize().y];
		collisionmap = new boolean[(int)_map.getSize().x][(int)_map.getSize().y];
		for (int _x = 0; _x < _map.getSize().x; _x += _step)
			for (int _y = 0; _y < _map.getSize().y; _y += _step){
				nodemap[(int)(_x / _step)][(int)(_y / _step)] = new Node(_x / _step, _y / _step);
				collisionmap[(int)(_x / _step)][(int)(_y / _step)] = _map.isWalkable(_x, _y);
			}
	}
	
	public boolean isAccessible(Vector2 _from, Vector2 _to){
		ArrayList<Node> frontwave = new ArrayList<Node>();
		Node _horsey;
		Node[][] _nm = nodemap.clone();
		
		//Align to path grid
		_from.set(Math.round(_from.x / step), Math.round(_from.y / step));
		_to.set(Math.round(_to.x / step), Math.round(_to.y / step));
		
		if (!collisionmap[(int)_from.x][(int)_from.y] || !collisionmap[(int)_to.x][(int)_to.y])
			return false;
		
		frontwave.add(_nm[(int)_from.x][(int)_from.y]);
		while (!frontwave.isEmpty()){
			_horsey = findBest(frontwave, _to);

			if (_horsey.x == _to.x && _horsey.y == _to.y)
				return true;
			
			//Let horsey grow!
			if (_horsey.x - 1 >= 0)
				letHorseyGrow(frontwave, _nm, (int)_horsey.x - 1, (int)_horsey.y);
			if (_horsey.x + 1 < _nm.length)
				letHorseyGrow(frontwave, _nm, (int)_horsey.x + 1, (int)_horsey.y);
			if (_horsey.y - 1 >= 0)
				letHorseyGrow(frontwave, _nm, (int)_horsey.x, (int)_horsey.y - 1);
			if (_horsey.y + 1 < _nm[0].length)
				letHorseyGrow(frontwave, _nm, (int)_horsey.x, (int)_horsey.y + 1);
			
			_horsey.check();
			/*System.out.println("Size:" + frontwave.size() + " Best:" + Math.round(_horsey.x) + "." +  Math.round(_horsey.y) + "\n Listing:");
			for (Node _letsfuck: frontwave){
				System.out.println("" + Math.round(_letsfuck.x) + "." +  Math.round(_letsfuck.y));
			}
			System.out.println("\n");*/
			frontwave.remove(_horsey);
		}
		return false;
	}
	
	private void letHorseyGrow(ArrayList<Node> _fw, Node[][] _nm, int _x, int _y){
		if (collisionmap[_x][_y]){
			if (!_nm[_x][_y].isChecked()){
				_nm[_x][_y].check();
				_fw.add(_nm[_x][_y]);
			}
		}
	}
	
	private Node findBest(ArrayList<Node> _list, Vector2 _to){
		Node best = _list.get(0);
		for (Node _runner: _list)
			if (_runner.dst(_to) < best.dst(_to))
				best = _runner;
		return best;
	}
}
