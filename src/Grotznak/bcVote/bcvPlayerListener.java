package Grotznak.bcVote;

import java.text.DecimalFormat;
import java.util.Hashtable;


import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class bcvPlayerListener {


	private bcVote plugin;
	private static Permission perm;
	private static Economy eco;

	public Votings dayvote = new Votings("day");
	public Votings nightvote = new Votings("night");
	public Votings sunvote = new Votings("sun");
	public Votings rainvote = new Votings("rain");

	private int permaOffset; 
	private Hashtable<String, String> CONFIG;
	private Hashtable<String, String> LANG;
	private Vault vault = null;

	public void config(Hashtable<String, String> CONFIG,Hashtable<String, String> LANG){
		this.CONFIG = CONFIG;
		this.LANG = LANG;
		this.vault = vault;
	}

	private World currentWorld = null;


	public boolean onPlayerCommand(CommandSender sender, Command command, String label, String[] args){
		perm = plugin.permission;
		eco = plugin.economy;
		Player player = (Player) sender;
		if (sender instanceof Player) {
			player = (Player) sender;

			sender.sendMessage("BlockCraft Voting:");
			currentWorld = player.getWorld();
		} else {
			plugin.printlog("onPlayerCommand - sender is not a player, skipping commands.");
			return false;
		}
		//sender.sendMessage("Event done");

		double nicetime =roundTwoDecimals ((player.getWorld().getTime()%24000)/1000);

		String[] split = args;
		if ((!label.equalsIgnoreCase("vote"))&&(!label.equalsIgnoreCase("bcvote"))) return false;
		
		
		if (split.length == 0 || (split.length == 1 && split[0].equalsIgnoreCase("help"))){
			sender.sendMessage(ChatColor.AQUA + LANG.get("VOTING_COMMANDS_HEAD"));			
			sender.sendMessage(ChatColor.AQUA + "/vote day " +LANG.get("VOTING_COMMANDS_VOTE_DESC_DAY"));
			sender.sendMessage(ChatColor.AQUA + "/vote night " +LANG.get("VOTING_COMMANDS_VOTE_DESC_NIGHT"));
			sender.sendMessage(ChatColor.AQUA + "/vote sun " +LANG.get("VOTING_COMMANDS_VOTE_DESC_SUN"));
			sender.sendMessage(ChatColor.AQUA + "/vote rain " +LANG.get("VOTING_COMMANDS_VOTE_DESC_RAIN"));
			sender.sendMessage(ChatColor.AQUA + "/vote undo " +LANG.get("VOTING_COMMANDS_VOTE_DESC_UNDO"));
			return true;
		}

		if(split[0].equalsIgnoreCase("info")){
			sender.sendMessage(ChatColor.AQUA + "BlockCraft-Voting created by Grotznak");
			sender.sendMessage(ChatColor.AQUA + LANG.get("TRANSLATION"));
			sender.sendMessage(ChatColor.AQUA + LANG.get("INFO_TIME") + " " + nicetime + " ("+player.getWorld().getName()+")");
			sender.sendMessage(ChatColor.AQUA + "visit us at www.blockcraft.de");
			return true;
		}

		if (split[0].equalsIgnoreCase("day")){
			if ( perm.playerHas(player, "bcvote.time") ) {			  

				long now = currentWorld.getTime();	
				now =  (now % 24000); // one day lasts 24000
				//sender.getServer().broadcastMessage(CONFIG.get("broadcast-votes"));

				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_DAY"));

				if (!isDay(now,permaOffset)){				
					if (dayvote.dovote(currentWorld,player,true,CONFIG,LANG,"Time")){					 
						sender.getServer().broadcastMessage(ChatColor.AQUA + LANG.get("VOTE_TIME_CHANGE"));
						currentWorld.setTime(permaOffset);	
						if (CONFIG.get("use-economy").equals("true")) {
							eco.withdrawPlayer(player.getDisplayName(),Double.valueOf(CONFIG.get("dayvote-cost")));
							sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_COST").replaceAll("%cost%",ChatColor.WHITE + CONFIG.get("dayvote-cost") +ChatColor.AQUA));
						}
						nightvote.dovote(currentWorld,player,false,CONFIG,LANG,"Time");
					}
				} else {
					sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_DAY_ALREADY"));
					sender.sendMessage(ChatColor.AQUA + LANG.get("INFO_TIME") + " "  + nicetime + " " +  LANG.get("INFO_TIME_CLOCK") + " ("+player.getWorld().getName()+")");			
					dayvote.dovote(currentWorld,player,true,CONFIG,LANG,"Time");
					nightvote.dovote(currentWorld,player,false,CONFIG,LANG,"Time");
				}

				if (CONFIG.get("broadcast-votes").equals("true")) {
					String broadcast = LANG.get("VOTE_BROADCAST");
					broadcast = broadcast.replaceAll("%yes%",""+ dayvote.yes.size());
					broadcast = broadcast.replaceAll("%no%",""+ dayvote.no.size());
					broadcast = broadcast.replaceAll("%vote%",ChatColor.WHITE +"day" +ChatColor.AQUA );
					sender.getServer().broadcastMessage(ChatColor.AQUA + broadcast);
				}
			}
			else {
				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_NO_PERMISSION"));
			}
		}

		if (split[0].equalsIgnoreCase("night")){
			if ( perm.playerHas(player, "bcvote.time") ) {
				long now = currentWorld.getTime();
				now =  (now % 24000); // one day lasts 24000

				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_NIGHT"));

				if (isDay(now,permaOffset)){				
					if (nightvote.dovote(currentWorld,player,true,CONFIG,LANG,"Time")){
						currentWorld.setTime(permaOffset+14000);
						if (CONFIG.get("use-economy").equals("true")) {
							eco.withdrawPlayer(player.getDisplayName(),Double.valueOf(CONFIG.get("nightvote-cost")));
							sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_COST").replaceAll("%cost%",ChatColor.WHITE + CONFIG.get("nightvote-cost") +ChatColor.AQUA));
						}
						sender.getServer().broadcastMessage(ChatColor.AQUA + LANG.get("VOTE_TIME_CHANGE"));
						dayvote.dovote(currentWorld,player,false,CONFIG,LANG,"Time");
					}
				} else {	
					sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_NIGHT_ALREADY"));
					sender.sendMessage(ChatColor.AQUA + LANG.get("INFO_TIME") + " "  + nicetime + " " +  LANG.get("INFO_TIME_CLOCK") + " ("+player.getWorld().getName()+")");			
					nightvote.dovote(currentWorld,player,true,CONFIG,LANG,"Time");
					dayvote.dovote(currentWorld,player,false,CONFIG,LANG,"Time");
				}

				if (CONFIG.get("broadcast-votes").equals("true")) {
					String broadcast = LANG.get("VOTE_BROADCAST");
					broadcast = broadcast.replaceAll("%yes%",""+ nightvote.yes.size());
					broadcast = broadcast.replaceAll("%no%",""+ nightvote.no.size());
					broadcast = broadcast.replaceAll("%vote%",ChatColor.WHITE +"night" +ChatColor.AQUA );
					sender.getServer().broadcastMessage(ChatColor.AQUA + broadcast);
				}
			}
			else {
				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_NO_PERMISSION"));
			}
		}

		if (split[0].equalsIgnoreCase("sun")){				
			if (perm.playerHas(player, "bcvote.weather") ) {	
				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_SUN") );
				if (!isSun(currentWorld)){				
					if (sunvote.dovote(currentWorld,player,true,CONFIG,LANG,"Weather")){
						currentWorld.setWeatherDuration(1);
						currentWorld.setStorm(false);
						if (CONFIG.get("use-economy").equals("true")) {
							eco.withdrawPlayer(player.getDisplayName(),Double.valueOf(CONFIG.get("sunvote-cost")));
							sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_COST").replaceAll("%cost%",ChatColor.WHITE + CONFIG.get("sunvote-cost") +ChatColor.AQUA));
						}					
						sender.getServer().broadcastMessage(ChatColor.AQUA + LANG.get("VOTE_WEATHER_CHANGE"));
						rainvote.dovote(currentWorld,player,false,CONFIG,LANG,"Weather");
					}
				} else {				 
					sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_SUN_ALREADY"));
					sender.sendMessage(ChatColor.AQUA + LANG.get("INFO_TIME") + " "  + nicetime + " " +  LANG.get("INFO_TIME_CLOCK") + " ("+player.getWorld().getName()+")");			
					sunvote.dovote(currentWorld,player,true,CONFIG,LANG,"Weather");
					rainvote.dovote(currentWorld,player,false,CONFIG,LANG,"Weather");
				}
				if (CONFIG.get("broadcast-votes").equals("true")) {
					String broadcast = LANG.get("VOTE_BROADCAST");
					broadcast = broadcast.replaceAll("%yes%",""+ sunvote.yes.size());
					broadcast = broadcast.replaceAll("%no%",""+ sunvote.no.size());
					broadcast = broadcast.replaceAll("%vote%",ChatColor.WHITE +"sunshine" +ChatColor.AQUA );
					sender.getServer().broadcastMessage(ChatColor.AQUA + broadcast);				}
			}

			else {
				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_NO_PERMISSION"));
			}
		}
		if (split[0].equalsIgnoreCase("rain")){				
			if (perm.playerHas(player, "bcvote.weather")) {
				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_RAIN") );
				if (isSun(currentWorld)){				
					if (rainvote.dovote(currentWorld,player,true,CONFIG,LANG,"Weather")){
						currentWorld.setStorm(true);
						currentWorld.setWeatherDuration(Integer.parseInt(CONFIG.get("rain-duration")));
						if (CONFIG.get("use-economy").equals("true")) {
							eco.withdrawPlayer(player.getDisplayName(),Double.valueOf(CONFIG.get("rainvote-cost")));
							sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_COST").replaceAll("%cost%",ChatColor.WHITE + CONFIG.get("rainvote-cost") +ChatColor.AQUA));
						}
						sender.getServer().broadcastMessage(ChatColor.AQUA + LANG.get("VOTE_WEATHER_CHANGE"));
						sunvote.dovote(currentWorld,player,false,CONFIG,LANG,"Weather");
					}
				} else {				 
					sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_RAIN_ALREADY"));			
					sender.sendMessage(ChatColor.AQUA + LANG.get("INFO_TIME") + " "  + nicetime + " " +  LANG.get("INFO_TIME_CLOCK") + " ("+player.getWorld().getName()+")");
					rainvote.dovote(currentWorld,player,true,CONFIG,LANG,"Weather");
					sunvote.dovote(currentWorld,player,false,CONFIG,LANG,"Weather");
				}
				if (CONFIG.get("broadcast-votes").equals("true")) {
					String broadcast = LANG.get("VOTE_BROADCAST");
					broadcast = broadcast.replaceAll("%yes%",""+ rainvote.yes.size());
					broadcast = broadcast.replaceAll("%no%",""+ rainvote.no.size());
					broadcast = broadcast.replaceAll("%vote%",ChatColor.WHITE +"rain" +ChatColor.AQUA );
					sender.getServer().broadcastMessage(ChatColor.AQUA + broadcast);
				}
			}
			else {
				sender.sendMessage(ChatColor.AQUA + LANG.get("VOTE_NO_PERMISSION"));
			}
		}
		if (split[0].equalsIgnoreCase("undo")){	
			unregisterPlayerVotes(player);
		}
		return true;
	}



	public void unregisterPlayerVotes(Player p){

		dayvote.dovote(p.getWorld(),p,false,CONFIG,LANG,"Time");	
		nightvote.dovote(p.getWorld(),p,false,CONFIG,LANG,"Time");
		sunvote.dovote(p.getWorld(),p,false,CONFIG,LANG,"Weather");
		rainvote.dovote(p.getWorld(),p,false,CONFIG,LANG,"Weather");
	}

	private boolean isDay(long currenttime, int offset){
		return (currenttime < (12000 + offset)) && (currenttime > offset );
	}

	private boolean isSun(World world){
		if (world.hasStorm() || world.isThundering()){
			return false;
		} else {
			return true;
		}

	}

	double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
}
