package org.greenem.plugins.buildingserver;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import static org.greenem.plugins.buildingserver.SimpleLogger.log;
import static org.greenem.plugins.buildingserver.SimpleLogger.debug;

public class WorldLoadHelper {
	public static boolean worldLoaded(String name) {
		List<World> worlds = Bukkit.getWorlds();
		for (World w : worlds) {
			if(w.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean worldExists(String name) {
		File worldsDir = Bukkit.getWorldContainer();
		File theWorldFile = new File(worldsDir, name);
		return theWorldFile.exists() && theWorldFile.isDirectory(); // does isDirectory() always work fine?..
	}
	
	public static World loadWorld (String name) {
		World newWorld = Bukkit.createWorld(new WorldCreator(name));
		return newWorld;
//		if error return null;
	}
}
