package org.greenem.plugins.buildingserver;

import static org.greenem.plugins.buildingserver.SimpleLogger.log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
			if(isSenderAPlayer(sender)) {
				worldCommand(sender, args);
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("revert-last-changes")){
			if(isSenderAPlayer(sender)) {
				revertLastChangesCommand(sender, args);
			}
		}
		
		else if (cmd.getName().equalsIgnoreCase("mhreloadconfig")) {
			if(!isSenderAPlayerOrConsole(sender)) {
				return true;
			}
			loadConfig();
		}
		return false;
	}
	
	public void worldCommand(CommandSender sender, String[] args) {
		if(args.length==0) {
			sender.sendMessage(ChatColor.RED + "Put the world name there!");
			return;
		} else if(args.length>1) {
			sender.sendMessage(ChatColor.RED + "Put ONLY the world name there!");
			return;
		} else { // 1 arg
			String wName = args[0];
			boolean exists = WorldLoadHelper.worldExists(wName);
			if(exists) {
				WorldLoadHelper.loadWorld(wName);
				Player p = (Player) sender;
				p.teleport(new Location(Bukkit.getWorld(wName), 0.5, 101, 101)); // make better later
			}
			else {
				sender.sendMessage(ChatColor.RED + "The world doesn't exist!");
				return;
			}
		}
	}
	
	public void revertLastChangesCommand(CommandSender sender, String[] args) {
		Player p = (Player) sender;
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
	
	public void mhStopComand(Player p, String[] args) {
		p.sendMessage("This command is not coded yet");
	}
	
	public void RegisterAllCommands() {
		getCommand("mhproblems").setTabCompleter(new WorldCompleter());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().teleport(Locs.mainWorldSpawn);
	}
}