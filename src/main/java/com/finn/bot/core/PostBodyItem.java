package com.finn.bot.core;
/*
PostBodyItem.java - Class to serialize the Post Method body contents
Created by Lokesh H K, August 21, 2019.
Released into the repository BoT-Java-SDK.
*/
public class PostBodyItem extends Object{
	private String bot = null;
	
	public PostBodyItem(final String botValue){
		this.bot = botValue;
	}
	
	public String getBot(){
		return this.bot;
	}
}
