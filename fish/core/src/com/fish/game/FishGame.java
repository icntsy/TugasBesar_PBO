package com.fish.game;


import com.badlogic.gdx.Game;

public class FishGame extends Game
{
	public void create()
	{
		FishMenu cm = new FishMenu(this);
		setScreen( cm );

	}
}
