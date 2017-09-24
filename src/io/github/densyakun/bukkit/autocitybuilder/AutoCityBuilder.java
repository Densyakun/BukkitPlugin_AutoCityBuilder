package io.github.densyakun.bukkit.autocitybuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
public class AutoCityBuilder extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	@EventHandler
	public void BlockBreak(BlockBreakEvent e) {
		if (e.getPlayer().isSneaking()) {
			if (TerrainManager.aaa(e.getBlock().getType())) {
				TerrainManager.breakStand(e.getBlock());
			}
		}
	}
}
