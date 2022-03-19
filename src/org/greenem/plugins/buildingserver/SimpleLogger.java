package org.greenem.plugins.buildingserver;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class SimpleLogger {
	public static String pluginName = "Manhunt";
	public static String color = ""; // + ChatColor.WHITE;
	
	public static void log(String s) {
		s = color + "[" + pluginName + "]" + s;
		Bukkit.getConsoleSender().sendMessage(s);
	}
	
	public static void log(int s) {
		log(s + "");
	}
	
	public static void log(boolean s) {
		log(s + "");
	}
	
	public static void log(double s) {
		log(s + "");
	}
	
	public static void log(float s) {
		log(s + "");
	}
	
	public static void log(char s) {
		log(s + "");
	}
	
	public static void log(short s) {
		log(s + "");
	}
}
