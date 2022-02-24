package org.greenem.plugins.Manhunt.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class EmptyCompleter2Args implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		if(cmd.getName().equalsIgnoreCase("mhproblems")) {
			if(args.length==4) {
				return arguments;
			}
		}
		
		return null;
	}
	
}
