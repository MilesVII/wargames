package com.milesseventh.wargames;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;;

public class Utils {
	public static Vector2 WorldMousePosition = new Vector2(), UIMousePosition = new Vector2();//Updated via WG.java, update();
	public static Vector2 UIEnteringTapPosition = new Vector2();//Updated via WG.java, update();
	public static boolean isTouchJustReleased = false, confirmedTouchOccured = false;
	public static float confirmationTapDistance = 7;//px
	public static final int NULL_ID = -1;
	
	public static float projectX(float _len, float _dir){
		return (float)(_len * Math.cos(Math.toRadians(_dir)));
	}
	
	public static float projectY(float _len, float _dir){
		return (float)(_len * Math.sin(Math.toRadians(_dir)));
	}

	public static float remap(float x, float froml, float fromh, float tol, float toh){
		return (x - froml) / (fromh - froml) * (toh - tol) + tol;
	}
	
	public static boolean isBuildingAllowed(HeightMap _map, float _x, float _y){
		return _map.getMeta(_x, _y) < _map.getMetaThreshold();
	}
	
	private static Color cholder = new Color();
	public static Color getGradColor(Color _from, Color _to, float _percent){
		return cholder.set(MathUtils.lerp(_from.r, _to.r, _percent),
		                   MathUtils.lerp(_from.g, _to.g, _percent),
		                   MathUtils.lerp(_from.b, _to.b, _percent), 1);
	}
	public static Color getRoundGradColor(Color _from, Color _to, float _percent){
		return getGradColor(_from, _to, Math.round(_percent));
	}
	
	public static Color getGradColor(Color[] colors, float percent){
		float step = 1 / (float) (colors.length - 1);
		if (percent == 1)
			return colors[colors.length - 1];
		int c = Math.round((percent - percent % step) / step);
		return getRoundGradColor(colors[c], colors[c + 1], (percent % step) / step);
	}
	
	private static final float DBG_MIN_CITY_H = .10f, DBG_MAX_CITY_H = .32f;
	public static Vector2 debugFindAPlaceForStructure(HeightMap _map){
		Vector2 _place;
		Random _r = new Random();
		do {
			_place = new Vector2(_r.nextInt(_map.getWidth()), _r.nextInt(_map.getHeight()));
		} while(_map.getMeta(_place.x, _place.y) > DBG_MAX_CITY_H || _map.getMeta(_place.x, _place.y) < DBG_MIN_CITY_H);
		return _place;
	}
	
	public static boolean isOkToBuild(Vector2 position){
		return (findNearestStructure(null, position, null).position.dst2(position) > Heartstrings.STRUCTURE_BUILDING_MIN_DISTANCE2 &&
		        WG.antistatic.map.isWalkable(position.x, position.y));
	}
	
	public static void displaceSomewhereWalkable(Vector2 position, float distance, HeightMap map){
		Vector2 displace = Utils.getVector(distance, 0);
		int randomOffset = (int)(System.nanoTime() % 360);
		for (int i = randomOffset; i < randomOffset + 360; ++i){
			Vector2 newPlace    = Utils.getVector(position).add(displace.rotate(i % 360));
			Vector2 newPlaceMid = Utils.getVector(position).add(Utils.getVector(displace).scl(.5f));
			
			/*Somewhere in LibGDX
			 -- Hey, I'm building class for Vectors, working on operators
			 -- Cool!
			 -- Waddaya think, should operations on vectors change their state?
			 -- 'course yes! That's Java way, and Vectors are not primitive types right?
			 -- Oh yeah, they are objects. Objects are cool, aren't they, huh?
			 -- Yep, all those uncontrollable swarms of states, delightful!*/
			
			if (map.isWalkable(newPlace.x, newPlace.y) &&
			    map.isWalkable(newPlaceMid.x, newPlaceMid.y)){
				position.set(newPlace);
				break;
			}
		}
		System.out.println("Displace failed: Utils.java: displaceSomeWhereWalkable()"); //TODO: Debug info
	}
	
	public static Squad findNearestSquad(Faction f, Vector2 from, Squad except){
		if (f == null){
			Squad nearest = null;
			for (Faction faction: Faction.factions){
				Squad t = findNearestSquadCore(faction, from, except);
				if (nearest == null || from.dst2(t.position) < from.dst2(nearest.position))
					nearest = t;
			}
			return nearest;
		} else
			return findNearestSquadCore(f, from, except);
	}
	
	private static Squad findNearestSquadCore(Faction f, Vector2 from, Squad except){
		Squad minSquad = null;
		for (Squad to: f.squads)
			if (to != except && (minSquad == null || from.dst2(to.position) < from.dst2(minSquad.position)))
				minSquad = to;
		return minSquad;
	}
	
	public static void findStructuresWithinRadius2(ArrayList<Structure> results, Faction f, Vector2 from, float radius2, Structure except){
		results.clear();
		for (Structure to: f.structs)
			if (to != except && from.dst2(to.position) <= radius2)
				results.add(to);
	}

	public static Structure findNearestStructure(Faction f, Vector2 from, Structure except){
		if (f == null){
			Structure nearest = null;
			for (Faction faction: Faction.factions){
				Structure t = findNearestStructureCore(faction, from, except);
				if (nearest == null || from.dst2(t.position) < from.dst2(nearest.position))
					nearest = t;
			}
			return nearest;
		} else
			return findNearestStructureCore(f, from, except);
	}
	
	private static Structure findNearestStructureCore(Faction f, Vector2 from, Structure except){
		Structure minStructure = null;
		for (Structure to: f.structs)
			if (to != except && 
			    (minStructure == null || from.dst2(to.position) < from.dst2(minStructure.position)))
				minStructure = to;
		return minStructure;
	}
	
	public static float getAngle(Vector2 point){
		//return point.angle();
		return (float) Math.toDegrees((Math.atan2(point.y, point.x) > 0 ? Math.atan2(point.y, point.x) : Math.atan2(point.y, point.x) + Math.PI * 2));
	}
	
	public static void drawTrueArc(ShapeRenderer sr, Vector2 cnt, float radius, float start, float length, int segments){
		for (int i = 0; i < segments; ++i){
			double angle0 = Math.toRadians(start) + Math.toRadians(length) * ((i + 0) / (double)segments);
			double angle1 = Math.toRadians(start) + Math.toRadians(length) * ((i + 1) / (double)segments);
			sr.rectLine((float) Math.cos(angle0) * radius + cnt.x, (float) Math.sin(angle0) * radius + cnt.y, 
			            (float) Math.cos(angle1) * radius + cnt.x, (float) Math.sin(angle1) * radius + cnt.y, 3);
		}
	}
	
	public static String splitIntoLines(String victim, int line){
		if (victim.length() <= line)
			return victim;
		String[] words = victim.split(" ");
		int counter = 0;
		StringBuilder sb = new StringBuilder();
		/*for (int i = 0; i < words.length; ++i){
			if (counter + words[i].length() > line){
				if (counter != 0)
					sb.append('\b');
				sb.append('\n');
				counter = 0;             wrong
			}
			sb.append(words[i]);
			sb.append(' ');
			counter += words[i].length() + 1;
		}*/
		for (String word : words){
			sb.append(word);
			sb.append(' ');
			counter += word.length() + 1;
			if (counter > line){
				sb.append('\b');
				sb.append('\n');
				counter = 0;
			}
		}
		return sb.toString();
	}
	
	public static <T> boolean arrayContains(T[] a, T b){
		for (T x : a)
			if (x.equals(b))
				return true;
		return false;
	}
	
	private static final int VECTORS_IN_POOL = 128;
	public static Vector2[] vpool = new Vector2[VECTORS_IN_POOL];
	private static int vectorsCounter = 0, holder;
	public static Vector2 getVector(){//new Vector2() alternative
		return getVector(0, 0);
	}
	public static Vector2 getVector(Vector2 in){//cpy() alternative
		return getVector(in.x, in.y);
	}
	public static Vector2 getVector(float x, float y){//new Vector2(x, y) alternative
		if (vpool[vectorsCounter] == null)
			vpool[vectorsCounter] = new Vector2();
		vpool[vectorsCounter].x= x;
		vpool[vectorsCounter].y= y;
		holder = vectorsCounter;
		++vectorsCounter;
		vectorsCounter %= VECTORS_IN_POOL;
		return vpool[holder];
	}
	private static Color color = new Color();
	public static Color getColor(int r, int g, int b, int a){
		color.set(r / 255f, g / 255f, b / 255f, a / 255f);
		return color;
	}
}
