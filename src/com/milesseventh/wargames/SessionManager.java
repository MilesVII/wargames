package com.milesseventh.wargames;

public class SessionManager {
	private int turn = 0;
	private Fraction[] players;
	
	SessionManager (Fraction[] _players){
		players = _players;
	}
	
	public Fraction[] getFractions(){
		return players;
	}
	
	public Fraction getCurrent(){
		return players[turn];
	}
	
	public void endTurn(){
		turn++;
		if (turn == players.length)
			turn = 0;
	}
}
