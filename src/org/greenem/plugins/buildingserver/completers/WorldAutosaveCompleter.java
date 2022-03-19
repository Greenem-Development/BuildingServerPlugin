package org.greenem.plugins.buildingserver.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.greenem.plugins.buildingserver.ConfigData;

public class WorldAutosaveCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		//sender.sendMessage(commandLabel);
		if(cmd.getName().equalsIgnoreCase("world")) { // IS THIS NESSESSARY????
			if(args.length==1) {
				for (String s : ConfigData.worldsToBuildInArrayList) {
					if(!s.equals("put_world_names_like_this")) {
						arguments.add(s);
					}
				}
				return arguments;
			}
		}
		return null;
	}
	
}
