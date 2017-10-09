package me.Ronin1024.NPCPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public enum LogClass {
	TRUE,FALSE;
	 public static LogClass log(CommandSender sender, String string) {
		 if (sender!=null) {
			 sender.sendMessage("["+ChatColor.GREEN+"MinecraftPlugin"+ChatColor.WHITE+"] "+string);
		 }else {
			 Server server = Bukkit.getServer();
			 ConsoleCommandSender console = server.getConsoleSender();
			 console.sendMessage("["+ChatColor.GREEN+"MinecraftPlugin"+ChatColor.WHITE+"] "+string);			 
		 }
		 return TRUE;
	 }

}
