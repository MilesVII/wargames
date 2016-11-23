package com.milesseventh.wargames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;;

public class Utils {
	public static float projectX(float _len, float _dir){
		return (float)(_len * Math.cos(Math.toRadians(_dir)));
	}
	
	public static float projectY(float _len, float _dir){
		return (float)(_len * Math.sin(Math.toRadians(_dir)));
	}
	
	//Wrong
	public static float getAngle(Vector2 _from, Vector2 _to){
		float len =_from.dst(_to);
		if (len == 0)
			return 0;
		float projectedX = Math.abs(_to.x - _from.x);
		float angle = (float) Math.toDegrees(Math.asin(projectedX / len));
		if (_to.x - _from.x > 0)
			if (_to.y - _from.y > 0)
				//I
				return angle;
			else
				//IV
				return 360 - angle;
		else
			if (_to.y - _from.y > 0)
				//II
				return 180 - angle;
			else
				//III
				return 270 + angle;
	}

	/* Moved to Marching.java
	public static float[][] calculateMarchingGrid(Marchable _meta, Vector2 _size, float _step){
		float[][] grid = new float[(int)(Math.floor(_size.x / _step))][(int)(Math.floor(_size.y / _step))];
		for (int _x = 0; _x < grid.length; _x++)
			for (int _y = 0; _y < grid[0].length; _y++)
				grid[_x][_y] = _meta.getMeta(_x * _step, _y * _step);//metaValue(_cs, _map, _x * _step, _y * _step);
		return grid;
	}*/
	
	public static boolean isBuildingAllowed(NoisedMap _map, float _x, float _y){
		/*float min = WG.INFINITY, _dist;
		Marchable _ = null;
		for (Marchable march: _cs){
			_dist = Vector2.dst(march.getCircle().x, march.getCircle().y, _x, _y);
			if (_dist <= march.getCircle().radius && _dist < min){
				_ = march;//.getCircle().radius
				min = _dist;
			}
		}
		if (min != WG.INFINITY)
			return (min / _.getCircle().radius);
		else
			return WG.INFINITY;*/
		return _map.getMeta(_x, _y) < _map.getMetaThreshold();
	}

	public static Color getGradColor(Color _from, Color _to, float _percent){
		return new Color(MathUtils.lerp(_from.r, _to.r, _percent),
						MathUtils.lerp(_from.g, _to.g, _percent),
						MathUtils.lerp(_from.b, _to.b, _percent), 1);
	}
	
	public static Color getGradColor(Color[] _colors, float _percent){
		int _intervals =  _colors.length - 1;
		for (int c = 0; c < _intervals; c++)
			if (_percent < (1 / (float)_intervals * (float)(c + 1f)))
				return new Color(getGradColor(_colors[c], _colors[c + 1], _percent * _intervals - 1 / (float)_intervals * (float)c));
		return Color.BLACK;
	}
}
