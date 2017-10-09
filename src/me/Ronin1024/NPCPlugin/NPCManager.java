package me.Ronin1024.NPCPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayInArmAnimation;
import net.minecraft.server.v1_12_R1.PacketPlayInEntityAction;
import net.minecraft.server.v1_12_R1.PacketPlayInEntityAction.EnumPlayerAction;
import net.minecraft.server.v1_12_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import net.minecraft.server.v1_12_R1.AttributeInstance;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EnumMoveType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.MathHelper;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_12_R1.PacketPlayOutPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_12_R1.PacketPlayOutUpdateAttributes;

public class NPCManager {
	private CHumanNpc npc;
	private GameProfile gameprofile;
	private int entityID;
	double distancetoTarget;
	public NPCManager() {
		
	}
	
    public void destroy(){
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] {entityID});
        sendPacket(packet);
        removeFromTablist();
    }
    
	private void removeFromTablist(){
		boolean isOnline = false;
		for(Player p : Bukkit.getOnlinePlayers()){
			if(npc.getName().equals(p.getName()))
					isOnline = true;
		}
		if(isOnline)
			return;
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER);
		setValue(packet, "b", Arrays.asList(packet.new PlayerInfoData(npc.getProfile(), 0, null, null)));
		sendPacket(packet);

	}
	
	 public void setValue(Object obj,String name,Object value){
		 try{
			 Field field = obj.getClass().getDeclaredField(name);
			 field.setAccessible(true);
			 field.set(obj, value);
	     }catch(Exception e){}
	 }
	 
	 public void headRotation(float yaw,float pitch){	 
          PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(entityID, getFixRotation(yaw),getFixRotation(pitch) , true);
          PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
          setValue(packetHead, "a", entityID);
          setValue(packetHead, "b", getFixRotation(yaw));
          sendPacket(packet);
          sendPacket(packetHead);
         // LogClass.log(null, "yaw " + yaw + " - "+ getFixRotation(yaw) +" - " +getCompressedAngle(yaw) );
	    }
	    
	public void npcanim(int arg) {
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
        setValue(packet,"a",entityID);
        setValue(packet,"b",arg);
        sendPacket(packet);
        LogClass.log(null, "ID NPC "+ this.entityID + " Anim " + arg);
	}
	
    public void status(int status){
        PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus();
        setValue(packet, "a", entityID);
        setValue(packet, "b", (byte) status);
        sendPacket(packet);
        LogClass.log(null, "ID NPC "+ this.entityID + " Status " + status);
    }
    
    public void TurnToPlayer(Player player){
     	double[] PitchYaw = getPitchYaw(npc.getLocation(),player.getLocation());
    	float pitch = (float) PitchYaw[0];
    	float yaw = (float) PitchYaw[1];
    	
    	headRotation(yaw,pitch);
    	npc.setYaw(yaw);
    	npc.setPitch(pitch);
    }
    
   
    public void move (Player player) { 
    	//Это телепорт нужно делать через дельту
    	Location loc = newloc(npc.getLocation(),player.getLocation());
    	npc.move(loc.getX(),loc.getY(),loc.getZ());
    	npc.getLocation().add(loc);
    	byte X = (byte)(int) loc.getX();
    	byte Y = (byte)(int) loc.getY();
    	byte Z = (byte)(int) loc.getZ();
    	PacketPlayOutRelEntityMoveLook packet = new PacketPlayOutRelEntityMoveLook();
    	setValue(packet, "a", entityID);
    	setValue(packet, "b", Byte.valueOf(X));
    	setValue(packet, "c", Byte.valueOf(Y));
    	setValue(packet, "d", Byte.valueOf(Z));    	
    	setValue(packet, "e", Byte.valueOf(getFixRotation(npc.getYaw())));
    	setValue(packet, "f", Byte.valueOf(getFixRotation(npc.getPitch())));
    	setValue(packet, "g", Byte.valueOf((byte) 0));
    	setValue(packet, "h", 0);
    	
    	
    	
//    	PacketPlayOutRelEntityMove packet = new PacketPlayOutRelEntityMove(entityID,(long) loc.getX(), (long) loc.getY(), (long) loc.getZ(),true);	 

    	sendPacket(packet);
//    	double X=loc.getX();
//    	double Y=loc.getY();
//    	double Z=loc.getZ();	
//    	LogClass.log(null, "X " +X+" Y "+Y+" Z " + Z + " tfp " + toFxdPnt( loc.getX()));    	
    }
    
    public Location newloc(Location from, Location to) {
        double dX = from.getX() - to.getX();
        double dY = from.getY();
        double dZ = from.getZ() - to.getZ();
        if (dX>0) {dX=from.getX()-1;}else{dX=from.getX()+1;}
        if (dZ>0) {dZ=from.getZ()-1;}else{dZ=from.getZ()+1;}        
    	return new Location(from.getWorld(),dX,dY,dZ);
    }
    
	private int toFxdPnt(double value){
		return (int) Math.floor(value * 1.0D);
	}
    
	public void sendPacket(Packet<?> packet) {
	        for (Player player : Bukkit.getOnlinePlayers()){
        	PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        	connection.sendPacket(packet);
       }
	}
	
	public void update() {
		for (Player player : Bukkit.getOnlinePlayers()){
			TurnToPlayer(player);
			if (distancetoTarget>3) {move(player);}
		}
		//move();
		//Bukkit.broadcastMessage("move npc " + npc.getId()); 
        
	}
	
    public void changeSkin(){
//        String value = "eyJ0aW1lc3RhbXAiOjE0NDI4MzY1MTU1NzksInByb2ZpbGVJZCI6IjkwZWQ3YWY0NmU4YzRkNTQ4MjRkZTc0YzI1MTljNjU1IiwicHJvZmlsZU5hbWUiOiJDb25DcmFmdGVyIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xMWNlZDMzMjNmYjczMmFjMTc3MTc5Yjg5NWQ5YzJmNjFjNzczZWYxNTVlYmQ1Y2M4YzM5NTZiZjlhMDlkMTIifX19";
//        String signature = "tFGNBQNpxNGvD27SN7fqh3LqNinjJJFidcdF8LTRHOdoMNXcE5ezN172BnDlRsExspE9X4z7FPglqh/b9jrLFDfQrdqX3dGm1cKjYbvOXL9BO2WIOEJLTDCgUQJC4/n/3PZHEG2mVADc4v125MFYMfjzkznkA6zbs7w6z8f7pny9eCWNXPOQklstcdc1h/LvflnR+E4TUuxCf0jVsdT5AZsUYIsJa6fvr0+vItUXUdQ3pps0zthObPEnBdLYMtNY3G6ZLGVKcSGa/KRK2D/k69fmu/uTKbjAWtniFB/sdO0VNhLuvyr/PcZVXB78l1SfBR88ZMiW6XSaVqNnSP+MEfRkxgkJWUG+aiRRLE8G5083EQ8vhIle5GxzK68ZR48IrEX/JwFjALslCLXAGR05KrtuTD3xyq2Nut12GCaooBEhb46sipWLq4AXI9IpJORLOW8+GvY+FcDwMqXYN94juDQtbJGCQo8PX670YjbmVx7+IeFjLJJTZotemXu1wiQmDmtAAmug4U5jgMYIJryXMitD7r5pEop/cw42JbCO2u0b5NB7sI/mr4OhBKEesyC5usiARzuk6e/4aJUvwQ9nsiXfeYxZz8L/mh6e8YPJMyhVkFtblbt/4jPe0bs3xSUXO9XrDyhy9INC0jlLT22QjNzrDkD8aiGAopVvfnTTAug=";
        
        String signature = "dU+CIkqJYU3gj4NTwl6Jaf2Q195zdIwt6Qd9a0unAOQ1xf1FbrJ+QFHCcpQ1TtDT5nnn8SkQIiEELECs23wHsN4SCKwn4Hy4CsvumO3wf8F3GITS6alMQ1XT4/WGqTpiJlRpHq+2subt7vD5Pzmv5ajWo9vjER4XqRpP2OgTM5nnZdTnLPXgsYQesQ6ocymGgAwUZm6HQXfuyPsosL1UF/BLv6eaXh6dWRK653i73vv+Kho3h7L+Ewyh80tFY8ax9YgxP3fZ6mKiNvct5K6b0UZOy5incyRRXaDjEoFnZdNPc4Izy4bAydS9QSyiUgaEqbt1h1kqc381ZHiw6KTFEHYeOnFu0KUukIs86MvafV5SS7R5LJWLTfk2L96hpT73ndrQnugOOqDkF5XUq6U6b3hyj+00Ozl5CjLStwdhDjNmhdJcVZqAbmmiIDKS8Erv+eDVPHDeze/PRMgIPMj2bN0J7xdoIRWOdo0lSFk2iYdfhF+qZ+uk+Qw3F8r1uvV4y/x0X1sohHIf1AaRS5D9rA8DAB4/NyDDAnGwAWIf06oAXKErg4ZfWX4eHv1TKKMJBQWGBISD46VySvKX0QbRKV5icc/QqV9eOCXotvXEDJQJQV+oCYBkS/oHzCA1bvCAVsmmnMivyeVC4GpzR5eoBPghF2kYExPNiyh4HA1n9wc=";
        String value = "eyJ0aW1lc3RhbXAiOjE0NDcxNzQ2NjUxNjEsInByb2ZpbGVJZCI6IjYxNjk5YjJlZDMyNzRhMDE5ZjFlMGVhOGMzZjA2YmM2IiwicHJvZmlsZU5hbWUiOiJEaW5uZXJib25lIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZDZiZTkxNWIyNjE2NDNmZDEzNjIxZWU0ZTk5YzllNTQxYTU1MWQ4MDI3MjY4N2EzYjU2MTgzYjk4MWZiOWEifSwiQ0FQRSI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNmNjg4ZTBlNjk5YjNkOWZlNDQ4YjViYjUwYTNhMjg4ZjljNTg5NzYyYjNkYWU4MzA4ODQyMTIyZGNiODEifX19";
        gameprofile.getProperties().put("textures", new Property("textures", value, signature));
}
	
	public void addNPC(String name,Player player) {
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
        gameprofile = new GameProfile(UUID.randomUUID(), name);
        changeSkin();
        this.npc = new CHumanNpc(server, world, gameprofile,new PlayerInteractManager(world), player);
        //PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, npc));
        sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        //sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\"Новый NCP создан!\"}")));
        this.entityID = this.npc.getId();
        LogClass.log(null, "ID NPC "+ this.entityID);
	}

	
    public int getFixLocation(double pos){
        return (int) MathHelper.floor(pos * 32.0D);
    }

    public byte getFixRotation(float yawpitch){
        return (byte) ((int) (yawpitch * 256.0F / 360.0F));
    }
    
    private byte getCompressedAngle(float value) {
        return (byte) ((value * 256.0F) / 360.0F);
    }
    
    
    private double[] getPitchYaw(Location from, Location to){
    	//LogClass.log(null,"npc X "  +from.getX() + " player X " + to.getX());
        double dX = from.getX() - to.getX();
        double dY = from.getY() - to.getY();
        double dZ = from.getZ() - to.getZ();
        double distance = Math.sqrt(dZ * dZ + dX * dX + dY * dY);
        double flatDistance = Math.sqrt(dX * dX + dZ * dZ);
        distancetoTarget = flatDistance;
        double pitch = Math.asin(dY / distance)/ Math.PI * 180;
        double yaw = Math.acos(dZ / flatDistance)/ Math.PI * 180;
        if (dX<0) {yaw -= 180;}else {yaw = 180-yaw;}
        
//        LogClass.log(null, "dx " + dX + " dy " + dY + " dZ " + dZ + " dist " + distance + " flat " + flatDistance);
//        LogClass.log(null, "pitch " + pitch + " yaw " + yaw);
     
        return new double[]
        { pitch , yaw };
    }
}
