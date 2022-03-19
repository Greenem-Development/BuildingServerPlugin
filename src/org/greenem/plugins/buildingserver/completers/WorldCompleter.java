package org.greenem.plugins.buildingserver.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class WorldCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		//sender.sendMessage(commandLabel);
		if(cmd.getName().equalsIgnoreCase("mhproblems")) {
			if(args.length==1) {
				arguments.add("give_compass");
				return arguments;
			}
		}
		return null;
	}
	
}
