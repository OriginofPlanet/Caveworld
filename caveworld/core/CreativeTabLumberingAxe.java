package caveworld.core;

import caveworld.item.CaveItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabLumberingAxe extends CreativeTabs
{
	public CreativeTabLumberingAxe()
	{
		super(Caveworld.MODID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTabLabel()
	{
		return CaveItems.lumbering_axe.getUnlocalizedName() + ".name";
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getTranslatedTabLabel()
	{
		return getTabLabel();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Item getTabIconItem()
	{
		return CaveItems.lumbering_axe;
	}
}