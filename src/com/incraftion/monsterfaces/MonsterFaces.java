package com.incraftion.monsterfaces;
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



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class MonsterFaces extends JavaPlugin {

	private ProtocolManager protocolManager;
	private EntityEquipmentListener listener;
	private Yaml yaml = new Yaml(new Constructor(List.class));

	public static enum configType {DOUBLE, BOOLEAN, STRING, STRINGLIST};
	@SuppressWarnings("serial")
	public static final Map<String, configType> configKeys = new HashMap<String, configType>(){
		{
			put("playernames", configType.STRINGLIST);
			put("rate", configType.DOUBLE);
		}
	};
	public static final String configKeysString = implode(configKeys.keySet(), ", ");
	
	public FileConfiguration config;
	public String playername;
	
	public void initConfig(boolean copyDefaults) {
		config = getConfig();
		if (copyDefaults) {
			config.options().copyDefaults(true);
			saveDefaultConfig();
		}
	}
	
	@Override
	public void onEnable(){
		initConfig(true);
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		listener = new EntityEquipmentListener(getServer(), getLogger());
		listener.addListener(protocolManager, this);
	}
	
	@Override
	public void onDisable() {
		protocolManager.removePacketListeners(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (!cmd.getName().equalsIgnoreCase("MonsterFaces")) {
			return false;
		}
		if (args.length == 0) {
			sender.sendMessage("["+label+"] Subcommands: config");
			return true;
		}
		if (args[0].equalsIgnoreCase("config")) {
			if (args.length == 1) {
				sender.sendMessage("["+label+":config] Subcommands: get, set, reload");
				return true;
			}
			if (args[1].equalsIgnoreCase("get") || args[1].equalsIgnoreCase("view")) {
				if (!sender.hasPermission("monsterfaces.config.get")) {
					sender.sendMessage("["+label+":config:get] You don't have permission to use that command");
					return true;
				}
				if (args.length == 3) {
					String key = args[2].toLowerCase();
					sender.sendMessage("["+label+":config:get] "+key+": "+config.get(key));
				} else {
					sender.sendMessage("["+label+":config:get] Syntax: "+label+" config get <variable>");
					sender.sendMessage("["+label+":config:get] Config variables: "+configKeysString);
				}
				return true;
			} else if (args[1].equalsIgnoreCase("set")) {
				if (!sender.hasPermission("monsterfaces.config.set")) {
					sender.sendMessage("["+label+":config:set] You don't have permission to use that command");
					return true;
				}
				if (args.length == 3) {
					String key = args[2].toLowerCase();
					config.set(key, null);
					saveConfig();
					sender.sendMessage("["+label+":config:set] "+key+": "+config.get(key));
				} else if (args.length >= 4) {
					String key = args[2].toLowerCase();
					String value = args[3].toLowerCase();
					boolean keyFound = false;
					
					for (String keySet : configKeys.keySet()) {
						if (key.equals(keySet.toLowerCase())) {
							keyFound = true;
							switch (configKeys.get(keySet.toLowerCase())) {
							case BOOLEAN:
								if (value.equals("false") || value.equals("no") || value.equals("0")) {
									config.set(key, false);
								} else {
									config.set(key, true);
								}
								break;
							case DOUBLE:
								try {
									config.set(key, Double.parseDouble(value));
								} catch (NumberFormatException e) {
									sender.sendMessage("["+label+":config:set] ERROR: Can not convert "+value+" to a number");
								}
								break;
							case STRING:
								config.set(key, value);
								break;
							case STRINGLIST:
								String valuelong = "";
								for (int i = 3; i < args.length; i++) {
									valuelong += args[i] + " ";
								}
								List<String> valueList;
								try {
									valueList = yaml.loadAs(valuelong.trim(), List.class);
								} catch (YAMLException e) {
									sender.sendMessage("["+label+":config:set] Error setting "+key+", be sure to surround this setting in [square brackets] and [\"quotes\"] if you're using special characters");
									return true;
								}
								try {
									config.set(key, valueList);
								} catch (IllegalArgumentException e) {
									sender.sendMessage("["+label+":config:set] Error setting "+key);
									return true;
								}
								break;
							default:
								getLogger().warning("configType \""+configKeys.get(keySet.toLowerCase())+"\" unrecognised - this is a bug");
								break;
							}
							break;
						}
					}
					if (!keyFound) {
						config.set(key, value);
					}
					saveConfig();
					sender.sendMessage("["+label+":config:set] "+args[2].toLowerCase()+": "+config.get(args[2].toLowerCase()));
				} else {
					sender.sendMessage("["+label+":config:set] Syntax: "+label+" config set <variable> [value]");
					sender.sendMessage("["+label+":config:set] Config variables: "+configKeysString);
				}
				return true;
			} else if (args[1].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("monsterfaces.config.set")) {
					reloadConfig();
					initConfig(false);
					sender.sendMessage("["+label+":config:reload] Config reloaded");
				} else {
					sender.sendMessage("["+label+":config:reload] You don't have permission to use that command");
				}
				return true;
			} else {
				sender.sendMessage("["+label+":config:??] Invalid subcommand");
				return true;
			}
/*		} else if (args[0].equalsIgnoreCase("somethingelse")) {
			sender.sendMessage("["+label+":??] moo");
			return true;
*/		} else {
			sender.sendMessage("["+label+":??] Invalid subcommand");
			return true;
		}
	}
	
	public static String implode(Set<String> input, String glue) {
		int i = 0;
		StringBuilder output = new StringBuilder();
		for (String key : input) {
			if (i++ != 0) output.append(glue);
			output.append(key);
		}
		return output.toString();
	}
}
