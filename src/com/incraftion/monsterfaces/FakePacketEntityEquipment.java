package com.incraftion.monsterfaces;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class FakePacketEntityEquipment implements Runnable {
	private Plugin plugin;
	private ProtocolManager protocolManager;
	private Player player;
	private int uid;
	
	public FakePacketEntityEquipment(Plugin plugin, ProtocolManager protocolManager, Player player, int uid) {
		this.plugin = plugin;
		this.protocolManager = protocolManager;
		this.player = player;
		this.uid = uid;
	}

	@Override
	public void run() {
		PacketContainer packet = protocolManager.createPacket(0x05);
		Random nrand = new Random();
		int amt = plugin.getConfig().getStringList("playernames").size();
		int prand = nrand.nextInt(amt);
		List<String> playernames = plugin.getConfig().getStringList("playernames");
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		String skullName = playernames.get(prand);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		skullMeta.setOwner(skullName);
		skull.setItemMeta(skullMeta);
		try {
			packet.getSpecificModifier(int.class).write(0, uid).write(1, 4);
			packet.getItemModifier().write(0, skull);
			plugin.getLogger().info("Spawning dude#"+uid+" with "+((SkullMeta) skull.getItemMeta()).getOwner()+" skull");
			protocolManager.sendServerPacket(player, packet);
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Couldn't send fake EntityEquipment packet.", e);
		}
	}
	

}