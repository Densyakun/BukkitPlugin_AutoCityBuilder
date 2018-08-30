package io.github.densyakun.bukkit.autocitybuilder;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.xml.sax.SAXException;

import net.md_5.bungee.api.ChatColor;

public class AutoCityBuilder extends JavaPlugin implements Listener {

	public static String MSG_PREFIX;
	public static String MSG_ARGS_NOT_ENOUGH;

	@Override
	public void onLoad() {
		MSG_PREFIX = ChatColor.GOLD + "[" + getName() + "] ";
		MSG_ARGS_NOT_ENOUGH = MSG_PREFIX + ChatColor.RED + "パラメーターが足りません";
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		OSMManager.MAX_BBOX_SIZE = (float) config.getDouble("osm-max-bbox-size", OSMManager.MAX_BBOX_SIZE);
		OSMManager.MIN_Y = config.getInt("osm-min-y", OSMManager.MIN_Y);
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("osm")) {
			if (args.length == 0)
				sender.sendMessage(MSG_ARGS_NOT_ENOUGH);
			else if (args[0].equalsIgnoreCase("center")) {
				if (args.length == 1)
					sender.sendMessage(MSG_ARGS_NOT_ENOUGH);
				else if (args[1].equalsIgnoreCase("set")) {
					if (args.length < 4)
						sender.sendMessage(MSG_PREFIX + ChatColor.GREEN + "/osm center set [lat] [lon]");
					else if (args.length == 4) {
						if (sender instanceof Player) {
							try {
								Location loc = ((Player) sender).getLocation();
								double x = loc.getX();
								double z = loc.getZ();
								float lat = Float.valueOf(args[2]);
								float lon = Float.valueOf(args[3]);
								if (-90 > lat)
									lat = -90;
								else if (lat > 90)
									lat = 90;
								if (-180 > lon)
									lon = -180;
								else if (lon >= 180)
									lon = 180;
								OSMManager.setCenterPoint(x, z, lat, lon);
								sender.sendMessage(MSG_PREFIX + ChatColor.AQUA
										+ "Center point set as current location. x: " + x + " z: " + z);
							} catch (NumberFormatException e) {
								sender.sendMessage(MSG_PREFIX + ChatColor.RED + ""); //TODO
							}
						} else
							sender.sendMessage(MSG_PREFIX + ChatColor.RED + ""); //TODO
					} else if (args.length == 6) {
						try {
							double x = Double.valueOf(args[2]);
							double z = Double.valueOf(args[3]);
							float lat = Float.valueOf(args[4]);
							float lon = Float.valueOf(args[5]);
							if (-90 > lat)
								lat = -90;
							else if (lat > 90)
								lat = 90;
							if (-180 > lon)
								lon = -180;
							else if (lon >= 180)
								lon = 180;
							OSMManager.setCenterPoint(x, z, lat, lon);
							sender.sendMessage(MSG_PREFIX + ChatColor.AQUA + "Center point set as current location.");
						} catch (NumberFormatException e) {
							sender.sendMessage(MSG_PREFIX + ChatColor.RED + ""); //TODO
						}
					} else
						sender.sendMessage(MSG_PREFIX + ChatColor.RED + ""); //TODO
				} else if (args[1].equalsIgnoreCase("get")) {
					if (OSMManager.CenterPointIsEmpty())
						sender.sendMessage(MSG_PREFIX + ChatColor.GREEN + "Center point is empty."); //TODO
					else
						sender.sendMessage(MSG_PREFIX + ChatColor.GREEN + "Center point lat: "
								+ OSMManager.getCenterPointLat() + " lon: " + OSMManager.getCenterPointLon());
				} else
					sender.sendMessage(MSG_PREFIX + ChatColor.GREEN
							+ "/osm center set [lat] [lon] - Set a center point at the current location.\n"
							+ "/osm center get - Show center point location.");
			} else if (args[0].equalsIgnoreCase("build")) {
				if (sender instanceof Player) {
					if (args.length < 5)
						sender.sendMessage(MSG_PREFIX + ChatColor.GREEN + "/osm build [s] [w] [n] [e] - "); //TODO
					else {
						if (OSMManager.CenterPointIsEmpty())
							sender.sendMessage(MSG_PREFIX + ChatColor.RED + "Center point is empty."); //TODO
						else {
							sender.sendMessage(MSG_PREFIX + ChatColor.GREEN + "processing...");
							float s = Float.valueOf(args[1]);
							float w = Float.valueOf(args[2]);
							float n = Float.valueOf(args[3]);
							float e = Float.valueOf(args[4]);
							try {
								List<OSMNode> nodes = OSMManager.query_nodes_out(s, w, n, e);
								for (int a = 0; a < nodes.size(); a++)
									OSMManager.buildNode(((Player) sender).getWorld(), nodes.get(a));
								sender.sendMessage("nodes: " + nodes.size()); //TODO
							} catch (IOException | SAXException | ParserConfigurationException e1) {
								sender.sendMessage(MSG_PREFIX + ChatColor.RED + "Error: " + e1.getMessage());
							}
						}
					}
				}
			} else
				sender.sendMessage(MSG_PREFIX + ChatColor.GREEN
						+ "/osm center set [lat] [lon] - Set a center point at the current location.\n"
						+ "/osm center get - Show center point location.\n"
						+ "/osm build [s] [w] [n] [e] - ");//TODO
		}
		return true;
	}

	@EventHandler
	public void BlockBreak(BlockBreakEvent e) {
		if (e.getPlayer().isSneaking())
			if (TerrainManager.aaa(e.getBlock().getType()))
				TerrainManager.breakStand(e.getBlock());
	}
}
