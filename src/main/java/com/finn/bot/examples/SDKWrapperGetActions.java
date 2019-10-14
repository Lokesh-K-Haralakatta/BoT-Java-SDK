package com.finn.bot.examples;

import com.finn.bot.api.SDKWrapper;

public class SDKWrapperGetActions {

	public static void runSample() {
		System.out.println("Total actions retrieved: " + SDKWrapper.getActions().size());
	}

}
