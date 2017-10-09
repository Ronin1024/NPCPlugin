package me.Ronin1024.NPCPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumGamemode;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.PlayerList;
import net.minecraft.server.v1_12_R1.WorldServer;

public class CHumanNpc extends EntityPlayer {
    private int lastTargetId;
    private long lastBounceTick;
    private int lastBounceId;
    private float yaw;
    private float pitch;
    
    public PlayerConnection connection; 
    private Location location;
    
	public CHumanNpc(MinecraftServer server, WorldServer world, GameProfile profile, PlayerInteractManager playerinteractmanager,Player player) {
		 super(server, world, profile, playerinteractmanager);
		// PlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();
		 this.teleportTo(player.getLocation(), false);
		 NetworkManager nmgr = new NetworkManager(EnumProtocolDirection.SERVERBOUND);
		 connection = new PlayerConnection(server, nmgr, this);
		 playerinteractmanager.a((WorldServer) this.world);
		 playerinteractmanager.b(EnumGamemode.ADVENTURE);
		 this.location =  new Location(player.getWorld(),0,0,0);
		 this.location = player.getLocation();
		 //playerList.players.add(this);
	     //playerList.a(this, world);
	        lastTargetId = -1;
	        lastBounceId = -1;
	        lastBounceTick = 0;
	 }
	 
	   
	    public void move(double arg0, double arg1, double arg2) {
	        setPosition(arg0, arg1, arg2);
	    }
	    
	    public void setYaw(float arg) {
	        this.yaw=arg;
	    }
	    
	    public void setPitch(float arg) {
	        this.pitch=arg;
	    }
	    
	    public float getYaw() {
	        return this.yaw;
	    }
	    
	    public float getPitch() {
	        return this.pitch;
	    }
	    
	    public Location getLocation() {
	    	return this.location;
	    }
	    
	    @Override
	    public boolean a(EntityHuman entity) {
	        final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_RIGHTCLICKED);
	        Bukkit.getPluginManager().callEvent(event);

	        return super.a(entity);
	    }

	    public void b_(EntityHuman entity) {
	        if ((lastBounceId != entity.getId() || System.currentTimeMillis() - lastBounceTick > 1000) && entity.getBukkitEntity().getLocation().distanceSquared(getBukkitEntity().getLocation()) <= 1) {
	            final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_BOUNCED);
	            Bukkit.getPluginManager().callEvent(event);

	            lastBounceTick = System.currentTimeMillis();
	            lastBounceId = entity.getId();
	        }

	        if (lastTargetId == -1 || lastTargetId != entity.getId()) {
	            final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.CLOSEST_PLAYER);
	            Bukkit.getPluginManager().callEvent(event);
	            lastTargetId = entity.getId();
	        }

	        super.b(entity);
	    }

	    @Override
	    public void c(Entity entity) {
	        if (lastBounceId != entity.getId() || System.currentTimeMillis() - lastBounceTick > 1000) {
	            final EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_BOUNCED);
	            Bukkit.getPluginManager().callEvent(event);

	            lastBounceTick = System.currentTimeMillis();
	        }

	        lastBounceId = entity.getId();

	        super.c(entity);
	    }

	
}
