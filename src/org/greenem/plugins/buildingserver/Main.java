package org.greenem.plugins.buildingserver;

import static org.greenem.plugins.buildingserver.SimpleLogger.log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.greenem.plugins.buildingserver.completers.WorldCompleter;

public class Main extends JavaPlugin implements Listener {
	private Logger log = getLogger();
	public Plugin plugin = this;
	
	public File configsDirectory;
	public File mainConfigDirectory;
	
	public ArrayList<String> worldsToBuildInArrayList = new ArrayList<>();

	@Override
	public void onEnable() {
		log(ChatColor.GREEN + "Your " + SimpleLogger.pluginName + " plugin has been enabled!");
		
		getServer().getPluginManager().registerEvents(this, this);
		RegisterAllCommands();
		
		configsDirectory = new File(getServer().getWorldContainer(), "configs");
		mainConfigDirectory = new File(configsDirectory, "Building");
		
		mainConfigDirectory.mkdirs();
		
		loadConfig();
	}
	
	@Override
	public void onDisable() {
		log(ChatColor.RED + "Your " + SimpleLogger.pluginName + " plugin has been disabled.");
	}
	
	public void logCastEx(String currentKey) {
		log("Wrong value type for \"" + currentKey + "\" in the config");
	}
	
	public void loadConfig() {
		File f = new File(mainConfigDirectory, "mainconfig.yml");
		f.getParentFile().mkdirs();
		
		YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
		YamlConfiguration cOld = YamlConfiguration.loadConfiguration(f);
		String currentKey = null;

		try {
			currentKey = "building_worlds";
			if (c.get(currentKey) == null) {
				c.set(currentKey, List.of("put_world_names_like_this"));
			} else {
				try {
					ConfigData.worldsToBuildInArrayList = (ArrayList<String>) c.get(currentKey);
				} catch (ClassCastException e) {
					logCastEx(currentKey);
				}
			}
			/*currentKey = "allow-gm-command-for-ops";
			if (c.get(currentKey) == null) {
				c.set(currentKey, true);
			} else {
				try {
					ConfigData.allowGmCommandForOps = (boolean) c.get(currentKey);
				} catch (ClassCastException e) {
					logCastEx(currentKey);
				}
			}*/
		} catch (Throwable t) {
			log("Error while reading \"" + currentKey + "\" from the config");
		}
		
		// DONE
		
		boolean same = true;
	    for(String key : c.getKeys(false)) {
	        if(!cOld.contains(key)) {
	        	same = false;
	        }
	    }
	    
		if (!same) {
			try {
				c.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isSenderAPlayer(CommandSender sender) {
		if(sender instanceof ConsoleCommandSender) {
			sender.sendMessage(ChatColor.RED + "You can't do it through console!");
			return false;
		}
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You can use it only as a player!");
			return false;
		}
		return true;
	}
	
	public boolean isSenderAPlayerOrConsole(CommandSender sender) {
		if(!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(ChatColor.RED + "You can use it only as a player!");
			return false;
		}
		return true;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player p = null;
		String pName = "";
		try {
			p = (Player) sender;
			pName = p.getName();
		} catch (ClassCastException e) {}
		
		if(cmd.getName().equalsIgnoreCase("gm")){
			if(isSenderAPlayerOrConsole(sender)) {
				gmCommand(sender, args);
			}
		}
		if(cmd.getName().equalsIgnoreCase("heal")){
			if(isSenderAPlayerOrConsole(sender)) {
				healCommand(sender, p, pName, args);
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("world")){
			if(isSenderAPlayerOrConsole(sender)) {
				worldCommand(sender, args);
			}
		}
		if(cmd.getName().equalsIgnoreCase("hub")){
			if(isSenderAPlayer(sender)) {
				p.teleport(Locs.mainWorldSpawn);
			}
		}
		
		else if (cmd.getName().equalsIgnoreCase("reloadbuildingconfig")) {
			if(isSenderAPlayerOrConsole(sender)) {
				loadConfig();
			}
		}
		return false;
	}
	
	public void worldCommand(CommandSender sender, String[] args) {
		if(args.length==0) {
//			sender.sendMessage(ChatColor.RED + "Put the world name there!");
			sender.sendMessage(ChatColor.RED + "Select one of the options!");
			return;
//		} else if(args.length>1) {
//			sender.sendMessage(ChatColor.RED + "Put ONLY the world name there!");
//			return;
		} else {
			if(args[0].equals("teleport")) {
				if(isSenderAPlayer(sender)) {
					worldTeleport(sender, args);
				}
			} else if(args[0].equals("save")) {
				worldSave(sender, args);
			} else if(args[0].equals("rollback")) {
				revertLastChanges(sender, args);
			} else if(args[0].equals("autosave")) {
				worldAutosave(sender, args);
			} else if(args[0].equals("unload")) {
				unloadWorldCommand(sender, args);
			}
		}
	}
	
	private void worldTeleport(CommandSender sender, String[] args) {
		if(args.length>2) {
			sender.sendMessage(ChatColor.RED + "Put ONLY the world name there!");
			return;
		}
		String wName = args[1];
		boolean exists = WorldLoadHelper.worldExists(wName);
		if(exists) {
			boolean allowed = false;
			for (String s : ConfigData.worldsToBuildInArrayList) {
				if(s.equals(wName)) {
					allowed = true;
					break;
				}
			}
			if(allowed) {
				WorldLoadHelper.loadWorld(wName);
				Player p = (Player) sender;
				p.teleport(new Location(Bukkit.getWorld(wName), 0.5, 101, 0.5)); // make better later
				sender.sendMessage(ChatColor.GREEN + "You have been teleported!");
			}
			else {
				sender.sendMessage(ChatColor.RED + "You can't join that world!");
				return;
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "The world doesn't exist!");
			return;
		}
	}
	
	public void revertLastChanges(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		String wName = null;
		if(args.length==1) {
			wName = p.getWorld().getName();
		} else if(args.length>2) {
			sender.sendMessage(ChatColor.RED + "Put ONLY the world name there!");
			return;
		} else {
			wName = args[1];
		}
		
		if(wName.equals(Lines.mainWorldName)) {
			sender.sendMessage(ChatColor.RED + "You can't use it on the main world! (currently not supported)");
			return;
		}
		boolean exists = WorldLoadHelper.worldExists(wName);
		if(exists) {
			Map<Player, Location> locations = new HashMap<Player, Location>(); // if they log out, it's not my fault (what if the tp is called on an offline player?)
			World w = Bukkit.getWorld(wName);
			for (Player player : w.getPlayers()) {
				locations.put(player, player.getLocation().clone());
				player.teleport(Locs.mainWorldSpawn);
				player.sendMessage(ChatColor.GRAY + "You will be teleported back after the server reversed the changes of that world after the last save. The operation was requested by " + p.getName()); // what if he already logged out, npe lol
			}
			Bukkit.unloadWorld(w, false); // do we need autosaves?
			World newW = Bukkit.createWorld(new WorldCreator(wName));
			for (Player player : locations.keySet()) {
				Location newLoc = locations.get(player);
				newLoc.setWorld(newW);
				player.teleport(newLoc);
				player.sendMessage(ChatColor.GRAY + "You were be teleported back");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "The world doesn't exist!");
			return;
		}
	}
	
	public void worldSave(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		String worldName = null;
		World w = null;
		if(args.length==2) {
			worldName = args[1];
		}
		if(args.length>2) {
//			sender.sendMessage(ChatColor.RED + "Don't use any extra arguments there!");
			sender.sendMessage(ChatColor.RED + "Put only world name if you need it!");
			return;
		} else {
			if(worldName==null) {
				if(isSenderAPlayer(sender)) {
					w = p.getWorld();					
				} else return;
			} else {
				w = Bukkit.getWorld(worldName);
			}
			if(w==null) {
				sender.sendMessage(ChatColor.RED + "The world doesn't exist!");
				return;
			}
			w.save();
			sender.sendMessage(ChatColor.GREEN + "The world has been saved!");
		}
	}
	
	public void unloadWorldCommand(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		String worldName = null;
		World w = null;
		if(args.length==2) {
			worldName = args[1];
		}
		if(args.length>2) {
			sender.sendMessage(ChatColor.RED + "Put only world name if you need it!");
			return;
		} else {
			if(worldName==null) {
				if(isSenderAPlayer(sender)) {
					w = p.getWorld();					
				} else return;
			} else {
				w = Bukkit.getWorld(worldName);
			}
			if(ConfigData.worldsToBuildInArrayList.contains(worldName)) {
				if(w==null) {
					sender.sendMessage(ChatColor.RED + "That world is aldeady unloaded!");
					return;
				}
			}
			if(w==null) {
				sender.sendMessage(ChatColor.RED + "The world doesn't exist!");
				return;
			}
			for (Player player : w.getPlayers()) {
				player.teleport(Locs.mainWorldSpawn);
				player.sendMessage(ChatColor.GRAY + "You were teleported to the main world because the world you were in was unloaded by " + p.getName());
			}
			Bukkit.unloadWorld(w, true);
			sender.sendMessage(ChatColor.GREEN + "The world has been unloaded!");
		}
	}
	
	@SuppressWarnings("unused")
	public void worldAutosave(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		String worldName = null;
		World w = null;
		String msg = ChatColor.RED + "Put \"on\" or \"off\" there!";
		if(args.length==3) {
			worldName = args[2];
		}
		if(args.length>3) {
			sender.sendMessage(msg);
			return;
		} else {
			if(worldName==null) {
				if(isSenderAPlayer(sender)) {
					w = p.getWorld();					
				} else return;
			} else {
				w = Bukkit.getWorld(worldName);
			}
			boolean turnOn = false;
			if(args[1].equals("on")) {
				turnOn = true;
			} else if(args[1].equals("off")) {
				turnOn = false;
			} else {
				p.sendMessage(msg);
				return;
			}
			if(w==null) {
				sender.sendMessage(ChatColor.RED + "The world doesn't exist!");
				return;
			}
			if(w==null) {
				sender.sendMessage(ChatColor.RED + "The world doesn't exist!");
				return;
			}
			w.setAutoSave(turnOn);
			sender.sendMessage(ChatColor.GREEN + "The autosave in the specified world is now " + ChatColor.WHITE + args[1].toUpperCase() + ChatColor.GREEN + "!");
		}
	}
	
	public void gmCommand(CommandSender sender, String[] args) {
		Player p = getServer().getPlayerExact(sender.getName());
		if(sender instanceof ConsoleCommandSender || p.isOp() == true){
			if(args.length == 0){
				sender.sendMessage(ChatColor.RED + "Please, insert gamemode number.");
			}
			else if(args.length == 1){
				if(sender instanceof ConsoleCommandSender) {
					sender.sendMessage(ChatColor.RED + "You can only change the players' gamemodes from console!");
					return;
				}
				if(args[0].equalsIgnoreCase("0")){
					p.setGameMode(GameMode.SURVIVAL);
				}
				else if(args[0].equalsIgnoreCase("1")){
					p.setGameMode(GameMode.CREATIVE);
				}
				else if(args[0].equalsIgnoreCase("2")){
					p.setGameMode(GameMode.ADVENTURE);
				}
				else if(args[0].equalsIgnoreCase("3")){
					p.setGameMode(GameMode.SPECTATOR);
				}
				else {
					sender.sendMessage(ChatColor.RED + "Please, insert correct gamemode number.");
				}
			}
			else if(args.length == 2){
				if(getServer().getPlayerExact(args[1]) != null){
					if(args[0].equalsIgnoreCase("0")){
						getServer().getPlayerExact(args[1]).setGameMode(GameMode.SURVIVAL);
					}
					else if(args[0].equalsIgnoreCase("1")){
						getServer().getPlayerExact(args[1]).setGameMode(GameMode.CREATIVE);
					}
					else if(args[0].equalsIgnoreCase("2")){
						getServer().getPlayerExact(args[1]).setGameMode(GameMode.ADVENTURE);
					}
					else if(args[0].equalsIgnoreCase("3")){
						getServer().getPlayerExact(args[1]).setGameMode(GameMode.SPECTATOR);
					}
					else {
						sender.sendMessage(ChatColor.RED + "Please, insert correct gamemode number.");
					}
				}
				else {
					if(args[1].equals("0") || args[1].equals("1") || args[1].equals("2") || args[1].equals("3")) {
						sender.sendMessage(ChatColor.RED + "Usage for other players: /gm <number 0-3> <player>");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Such player isn't online.");
					}
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Please, type the command in such order: /gm <number 0-3> <player>");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "You can't use this command.");
		}
	}
	
	public String coloredName(String name) {
		String s = "";
		
		if(name.equals("[CONSOLE]")) {
			s += ChatColor.GOLD + "" + ChatColor.BOLD + name;
		} else {
			s += ChatColor.WHITE + "" + ChatColor.BOLD + name;
		}
		return s;
	}
	
	public String coloredHealSentence(String from, String to) {
		String s = "";
		
		if(to.equals("[CONSOLE]")) {
			s += ChatColor.GOLD + "" + ChatColor.BOLD;
		}
		else {
			s += ChatColor.WHITE + "" + ChatColor.BOLD;
		}
		s += to;
		
		s += ChatColor.RESET + " was healed";
		
		if(!from.equals(to)) {
			s += " by ";
			if(from.equals("[CONSOLE]")) {
				s += ChatColor.GOLD + "" + ChatColor.BOLD;
			} else {
				s += ChatColor.WHITE + "" + ChatColor.BOLD;
			}
			s += from;
		}
		return s;
	}
	
	public boolean logHealInfo = false; // false
	
	public void sendOthersTheAlert(String pAlert, String pName, String notifOthers) {
		if(!pAlert.equals(pName)) {
			Player pAlertP = getServer().getPlayerExact(pAlert);
			if(pAlertP!=null) {
				pAlertP.sendMessage(notifOthers);
			}
		}
	}
	
	public void healCommand(CommandSender sender, Player p, String pName, String[] args) {
		boolean console = sender instanceof ConsoleCommandSender;
		if(console) {
			pName = "[CONSOLE]";
		}
		else if(pName==null) {
			pName = p.getName();
		}
		if(p==null) {
			p = getServer().getPlayerExact(pName);
		}
		String pFrom = pName;
		String notifpersonal;
		String colorNameFrom = coloredName(pFrom);
		String notifOthers;
		if(args.length == 0) {
			if(sender instanceof ConsoleCommandSender) {
				sender.sendMessage(ChatColor.RED + "You can't heal yourself as the console!");
				return;
			}
			if(p!=null) {
				p.setHealth(20);
				p.setFoodLevel(20);
			}
			notifpersonal = ChatColor.GRAY + "You have been healed";
			notifOthers = colorNameFrom + ChatColor.RESET + "" + ChatColor.GRAY + " was healed";
			if(p!=null) {
				p.sendMessage(notifpersonal);
			}
			if(logHealInfo) log("notifpersonal: " + notifpersonal);
//			if(Hunters.contains(pName) || Speedrunners.contains(pName)) {
//				if(logHealInfo) log("notifOthers: " + notifOthers);
//				for (String pAlert : Hunters) {
//					sendOthersTheAlert(pAlert, pName, notifOthers);
//				}
//				for (String pAlert : Speedrunners) {
//					sendOthersTheAlert(pAlert, pName, notifOthers);
//				}
//			}
		}
		else if(args.length==1) {
			String pTo = args[0];
			Player pToP = getServer().getPlayerExact(pTo);
			if(pToP!=null || logHealInfo) {
				String colorNameTo = coloredName(pTo);
				boolean atLeastOneImportantPlayer = false; // Hunters.contains(pFrom) || Speedrunners.contains(pFrom) || Hunters.contains(pTo) || Speedrunners.contains(pTo);
				if(pToP!=null) {
					pToP.setHealth(20);
					pToP.setFoodLevel(20);
				}
				
				if(pTo.equals(pFrom)) {
					notifpersonal = ChatColor.GRAY + "You have been healed";
					if(p!=null) {
						p.sendMessage(notifpersonal);
					}
					notifOthers = colorNameFrom + ChatColor.RESET + "" + ChatColor.GRAY + " was healed";
				}
				else {
					String notifToThatOne = ChatColor.GRAY + "You have been healed by " + colorNameFrom;
					notifpersonal = ChatColor.GRAY + "You have healed " + colorNameTo;
					if(pToP!=null) {
						pToP.sendMessage(notifToThatOne);
						if(logHealInfo) log("notifToThatOne: " + notifToThatOne);
					}
					if(p!=null) {
						p.sendMessage(notifpersonal);
					}
					notifOthers = coloredHealSentence(colorNameFrom, colorNameTo);
				}
				
				if(logHealInfo) log("notifpersonal: " + notifpersonal);
				
				/*if(atLeastOneImportantPlayer) {
					if(logHealInfo) log("notifOthers: " + notifOthers);
					for (String pAlert : Hunters) {
						sendOthersTheAlert(pAlert, pName, notifOthers);
					}
					for (String pAlert : Speedrunners) {
						sendOthersTheAlert(pAlert, pName, notifOthers);
					}
				}*/
			}
			else {
				String problem = ChatColor.RED + "Such player isn't online.";
				if(logHealInfo) log("personal error to " + pName + ": " + problem);
				if(p!=null) {
					p.sendMessage(problem);
				}
			}
		}
		else {
			String problem = ChatColor.RED + "Please, type the command in such order: /heal <player>.";
			log("personal error to " + pName + ": " + problem);
			if(p!=null) {
				p.sendMessage(problem);
			}
		}
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}
	
	public void RegisterAllCommands() {
		getCommand("world").setTabCompleter(new WorldCompleter());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().teleport(Locs.mainWorldSpawn);
	}
}