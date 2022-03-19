package org.greenem.plugins.buildingserver.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.greenem.plugins.buildingserver.ConfigData;

public class WorldCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		//sender.sendMessage(commandLabel);
		if(cmd.getName().equalsIgnoreCase("world")) { // IS THIS NESSESSARY????
			if(args.length==1) {
				arguments.add("on");
				arguments.add("off");
				return arguments;
			}
		}
		return null;
	}
	
}
