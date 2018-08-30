package io.github.densyakun.bukkit.autocitybuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class TerrainManager {

	public static void breakStand(Block ground) {
		List<Block> blocks = a(ground, new ArrayList<Block>());
		for (int a = 0; a < blocks.size(); a++) {
			blocks.get(a).breakNaturally();
		}
	}

	public static boolean drop(Block block) {
		Collection<ItemStack> drops = block.getDrops();
		if (drops.size() == 0) {
			return block.breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
		} else {
			return block.breakNaturally();
		}
	}

	public static boolean drop(Block block, ItemStack item) {
		Collection<ItemStack> drops = block.getDrops(item);
		if (drops.size() != 0) {
			return block.breakNaturally(item);
		}
		return false;
	}

	public static boolean aaa(Material m) {
		return m == Material.DIRT || m == Material.GRASS || m == Material.SAND;
	}

	public static List<Block> a(Block block, List<Block> blocklist) {
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		Block left = block.getWorld().getBlockAt(x - 1, y, z);
		if (left != null && aaa(left.getType())) {
			for (int a = 0; a < blocklist.size(); a++) {
				if (blocklist.get(a).getX() == left.getX() && blocklist.get(a).getZ() == left.getZ()) {
					left = null;
					break;
				}
			}
			if (left != null) {
				Block up = block.getWorld().getBlockAt(left.getX(), left.getY() + 1, left.getZ());
				if (up != null && up.getType() == Material.AIR) {
					blocklist.add(left);
					a(left, blocklist);
				}
			}
		}
		Block right = block.getWorld().getBlockAt(x + 1, y, z);
		if (right != null && aaa(right.getType())) {
			for (int a = 0; a < blocklist.size(); a++) {
				if (blocklist.get(a).getX() == right.getX() && blocklist.get(a).getZ() == right.getZ()) {
					right = null;
					break;
				}
			}
			if (right != null) {
				Block up = block.getWorld().getBlockAt(right.getX(), right.getY() + 1, right.getZ());
				if (up != null && up.getType() == Material.AIR) {
					blocklist.add(right);
					a(right, blocklist);
				}
			}
		}
		Block back = block.getWorld().getBlockAt(x, y, z - 1);
		if (back != null && aaa(back.getType())) {
			for (int a = 0; a < blocklist.size(); a++) {
				if (blocklist.get(a).getX() == back.getX() && blocklist.get(a).getZ() == back.getZ()) {
					back = null;
					break;
				}
			}
			if (back != null) {
				Block up = block.getWorld().getBlockAt(back.getX(), back.getY() + 1, back.getZ());
				if (up != null && up.getType() == Material.AIR) {
					blocklist.add(back);
					a(back, blocklist);
				}
			}
		}
		Block forward = block.getWorld().getBlockAt(x, y, z + 1);
		if (forward != null && aaa(forward.getType())) {
			for (int a = 0; a < blocklist.size(); a++) {
				if (blocklist.get(a).getX() == forward.getX() && blocklist.get(a).getZ() == forward.getZ()) {
					forward = null;
					break;
				}
			}
			if (forward != null) {
				Block up = block.getWorld().getBlockAt(forward.getX(), forward.getY() + 1, forward.getZ());
				if (up != null && up.getType() == Material.AIR) {
					blocklist.add(forward);
					a(forward, blocklist);
				}
			}
		}
		return blocklist;
	}
}
