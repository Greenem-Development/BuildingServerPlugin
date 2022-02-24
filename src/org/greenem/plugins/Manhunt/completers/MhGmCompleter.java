package org.greenem.plugins.Manhunt.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MhGmCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		//sender.sendMessage(commandLabel);
		if(cmd.getName().equalsIgnoreCase("gm")) {
			if(args.length==1 && Bukkit.getOnlinePlayers().size()==0) {
				arguments.add("0");
				arguments.add("1");
				arguments.add("2");
				arguments.add("3");
				return arguments;	
			}
			if(args.length==2) {
				arguments.add("0");
				arguments.add("1");
				arguments.add("2");
				arguments.add("3");
				return arguments;
			}
		}
		return null;
	}
	
}
