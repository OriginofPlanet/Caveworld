package kegare.caveworld.core;

import kegare.caveworld.block.BlockPortalCaveworld;
import kegare.caveworld.block.ItemBlockPortalCaveworld;
import net.minecraft.block.BlockPortal;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class CaveBlock
{
	public static BlockPortal portalCaveworld;

	public static void load()
	{
		portalCaveworld = new BlockPortalCaveworld(Config.portalCaveworld, "portalCaveworld");

		GameRegistry.registerBlock(portalCaveworld, ItemBlockPortalCaveworld.class, "portalCaveworld");
		LanguageRegistry.addName(portalCaveworld, "Caveworld Portal");
	}
}