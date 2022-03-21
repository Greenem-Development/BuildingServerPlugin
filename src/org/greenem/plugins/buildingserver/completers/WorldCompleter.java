package org.greenem.plugins.buildingserver.completers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.greenem.plugins.buildingserver.ConfigData;

public class WorldCompleter implements TabCompleter {
	private boolean filterNotMatchingResults = true;

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		//sender.sendMessage(commandLabel);
		if(cmd.getName().equalsIgnoreCase("world")) { // IS THIS NESSESSARY????
			int currentLength = 0;
			if(args.length==1) {
				arguments.add("teleport");
				arguments.add("save");
				arguments.add("rollback");
				arguments.add("autosave");
				arguments.add("unload");
//				arguments.add("Х usage: <name from> <optional name to>"); //Л
//				arguments.add("Л usage: or like that.  ак лучше?");
				arguments = deleteNotMatching(arguments, args[currentLength]);
				return arguments;
			} else if(args.length==2) {
				currentLength = 1;
				if(args[0].equals("teleport")) {
					for (String s : ConfigData.worldsToBuildInArrayList) {
						if(!s.equals("put_world_names_like_this")) {
							arguments.add(s);
						}
					}
					arguments = deleteNotMatching(arguments, args[currentLength]);
					return arguments;
				}
				else if(args[0].equals("autosave")) {
					arguments.add("on");
					arguments.add("off");
					arguments = deleteNotMatching(arguments, args[currentLength]);
					return arguments;
				}
				else if(args[0].equals("unload") || args[0].equals("rollback") || args[0].equals("save")) {
					arguments = getLoadedWorldsList();
					arguments = deleteNotMatching(arguments, args[currentLength]);
					return arguments;
				}
				return arguments;
			}
			else if(args.length==3) {
				currentLength = 2;
				if(args[0].equals("autosave")) {
					if(args[1].equals("on") || args[1].equals("off")) {
						arguments = getLoadedWorldsList();
						arguments = deleteNotMatching(arguments, args[currentLength]);
						return arguments;
					}
				}
			}
			arguments = deleteNotMatching(arguments, args[currentLength]);
			return arguments;
		}
		return null;
	}
	
	private List<String> getLoadedWorldsList() {
		List<String> worlds = new ArrayList<>();
		for (World w : Bukkit.getWorlds()) {
			worlds.add(w.getName());
		}
		return worlds;
	}
	
	private List<String> deleteNotMatching(List<String> list, String currentText) {
		if(!filterNotMatchingResults) {
			return list;
		}
		for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
			String s = it.next();
		    if (currentText!=null && !s.startsWith(currentText)) {
		        it.remove();
		    }
		}
		return list;
	}
}
