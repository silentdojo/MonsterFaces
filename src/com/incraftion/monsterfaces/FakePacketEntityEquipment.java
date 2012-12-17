package com.incraftion.monsterfaces;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.shininet.bukkit.playerheads.Skull;

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
	
		try {
			packet.getSpecificModifier(int.class).write(0, uid).write(1, 4);
			packet.getItemModifier().write(0, new Skull(playernames.get(prand)).getItemStack());
			protocolManager.sendServerPacket(player, packet);
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Couldn't send fake EntityEquipment packet.", e);
		}
	}
	

}