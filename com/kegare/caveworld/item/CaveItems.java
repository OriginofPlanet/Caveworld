/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.oredict.OreDictionary;

import com.kegare.caveworld.block.CaveBlocks;
import com.kegare.caveworld.core.Config;
import com.kegare.caveworld.recipe.RecipeMiningPickaxe;

import cpw.mods.fml.common.registry.GameRegistry;

public class CaveItems
{
	public static final ItemCavenium cavenium = new ItemCavenium("cavenium");
	public static final ItemMiningPickaxe mining_pickaxe = new ItemMiningPickaxe("pickaxeMining");
	public static final ItemOreCompass ore_compass = new ItemOreCompass("oreCompass");

	public static void registerItems()
	{
		if (Config.cavenium)
		{
			GameRegistry.registerItem(cavenium, "cavenium");

			ItemStack item = new ItemStack(cavenium, 1, 0);
			OreDictionary.registerOre("cavenium", item);
			OreDictionary.registerOre("gemCavenium", item);

			ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, new WeightedRandomChestContent(item, 3, 5, 10));
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(item, 3, 5, 10));

			item = new ItemStack(cavenium, 1, 1);
			OreDictionary.registerOre("refinedCavenium", item);
			OreDictionary.registerOre("gemRefinedCavenium", item);

			ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, new WeightedRandomChestContent(item, 1, 3, 3));
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(item, 1, 3, 3));

			GameRegistry.addShapelessRecipe(new ItemStack(cavenium, 9, 0), new ItemStack(CaveBlocks.cavenium_ore, 1, 2));
			GameRegistry.addShapelessRecipe(new ItemStack(cavenium, 9, 1), new ItemStack(CaveBlocks.cavenium_ore, 1, 3));

			GameRegistry.addShapedRecipe(new ItemStack(CaveBlocks.cavenium_ore, 1, 2),
				"CCC", "CCC", "CCC",
				'C', new ItemStack(cavenium, 1, 0)
			);
			GameRegistry.addShapedRecipe(new ItemStack(CaveBlocks.cavenium_ore, 1, 3),
				"CCC", "CCC", "CCC",
				'C', new ItemStack(cavenium, 1, 1)
			);

			if (Config.refinedCaveniumCraftRecipe)
			{
				GameRegistry.addShapelessRecipe(new ItemStack(cavenium, 1, 1),
					new ItemStack(cavenium, 1, 0),
					new ItemStack(Items.dye, 1, 7), Items.quartz, Items.glowstone_dust
				);
			}

			FurnaceRecipes.smelting().func_151394_a(new ItemStack(CaveBlocks.cavenium_ore, 1, 0), new ItemStack(cavenium, 1, 0), 0.5F);
			FurnaceRecipes.smelting().func_151394_a(new ItemStack(CaveBlocks.cavenium_ore, 1, 1), new ItemStack(cavenium, 1, 1), 0.75F);
		}

		if (Config.pickaxeMining)
		{
			GameRegistry.registerItem(mining_pickaxe, "mining_pickaxe");

			OreDictionary.registerOre("pickaxeMining", mining_pickaxe);
			OreDictionary.registerOre("miningPickaxe", mining_pickaxe);

			GameRegistry.addRecipe(RecipeMiningPickaxe.instance);
		}

		if (Config.oreCompass)
		{
			GameRegistry.registerItem(ore_compass, "ore_compass");

			OreDictionary.registerOre("compassOre", ore_compass);

			GameRegistry.addShapedRecipe(new ItemStack(ore_compass),
				" C ", "CXC", " C ",
				'C', new ItemStack(cavenium, 1, 1),
				'X', Items.compass
			);
		}
	}
}