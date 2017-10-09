package me.Ronin1024.NPCPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCPlugin extends JavaPlugin implements Listener{
	private NPCManager Npcmgr;
	private void broadcast(String string) {
		Bukkit.broadcastMessage(string);
	}
	
	
	public void onEnable(){ 
		LogClass.log(getServer().getConsoleSender(),"Your plugin has been enabled!");
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(this, this);
        this.Npcmgr = new NPCManager();
            	
	}

	public void onDisable(){ 
		LogClass.log(getServer().getConsoleSender(),"Your plugin has been disabled.");
		 this.Npcmgr.destroy();

	}
	
	public void setTimedTask() {
		BukkitRunnable runnable = new BukkitRunnable(){
			@Override
			public void run (){
				NPCPlugin.this.Npcmgr.update();
				//Bukkit.broadcastMessage("update npc"); 				
				//600L - 30 секунд
			}
		};
    	//runnable.runTaskTimer(this, 1L, 600L);
		runnable.runTaskTimer(this, 1L, 2L);
	}
	
	
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("summonnpc")) {
            LogClass.log(sender, "Summoned NPC!");  
            this.Npcmgr.addNPC("Nome", (Player) sender);
            setTimedTask();
            return true;
        }
        if (command.getName().equalsIgnoreCase("npcstart")) {
            LogClass.log(sender, "Started NPC!");
        	setTimedTask();            
            return true;
        }
        if (command.getName().equalsIgnoreCase("npcmove")) {
            LogClass.log(sender, "NPC MOVE!");
            this.Npcmgr.move((Player) sender);          
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("npcanim")) {
            LogClass.log(sender, "npcanim!");
            this.Npcmgr.npcanim((int) Integer.parseInt(args[0]));            
            return true;
        }
        if (command.getName().equalsIgnoreCase("npcstatus")) {
            LogClass.log(sender, "npcanim!");
            this.Npcmgr.status((int) Integer.parseInt(args[0]));            
            return true;
        }

        return false;
    }
    
	@EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	LogClass.log(player,"Welcome."); 
    }
	
}
