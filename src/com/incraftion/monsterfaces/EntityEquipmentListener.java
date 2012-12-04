/* MonsterFaces - Bukkit plugin
 * Copyright (C) 2012 meiskam
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.incraftion.monsterfaces;

import java.util.logging.Level;
import java.util.logging.Logger;

//import net.minecraft.server.NBTTagCompound;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.shininet.bukkit.playerheads.Skull;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

public class EntityEquipmentListener {

	private final Server server;
	private final Logger logger;

	public EntityEquipmentListener(Server server, Logger logger) {
		this.server = server;
		this.logger = logger;
	}
	
	public void addListener(final ProtocolManager protocolManager, final JavaPlugin myPlugin) {

		protocolManager.addPacketListener(new PacketAdapter(myPlugin, ConnectionSide.SERVER_SIDE, 0x05, 0x18) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				
				try {
					switch (event.getPacketID()) {
					case 0x05:
						if (packet.getSpecificModifier(int.class).size() >= 2 && packet.getItemModifier().size() >= 1) {
							int id = packet.getSpecificModifier(int.class).read(0);
							int slot = packet.getSpecificModifier(int.class).read(1);
							if (slot == 4) {
								ItemStack item = packet.getItemModifier().read(0);
								if (item == null) {
									event.setCancelled(true);
									return;
								}
								for (Entity ent : event.getPlayer().getNearbyEntities(128, 128, 128)) {
									//logger.info(player.getEntityId()+" == "+id+" ?");
									if (ent.getType().getTypeId() == id && ent instanceof Zombie) {
										item = (new Skull(plugin.getConfig().getString("playername"))).getItemStack();
										break;
									}
								}
							}
						}
						break;
					    
					case 0x18:
						if (packet.getSpecificModifier(int.class).size() >= 2 && (packet.getSpecificModifier(int.class).read(1) == 54)) {
							server.getScheduler().scheduleSyncDelayedTask(myPlugin, 
									new FakePacketEntityEquipment(myPlugin, protocolManager, event.getPlayer(), packet.getSpecificModifier(int.class).read(0)) );
						}
						break;
						
//					case 0x00:
//						break;
					}
				
				} catch (FieldAccessException e) {
					logger.log(Level.SEVERE, "Couldn't access field.", e);
				}
			}
		});
	}
}