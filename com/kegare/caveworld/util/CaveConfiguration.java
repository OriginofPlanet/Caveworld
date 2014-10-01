/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.TreeMap;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Maps;

public class CaveConfiguration extends Configuration implements Comparator<String>
{
	public CaveConfiguration() {}

	public CaveConfiguration(File file)
	{
		super(file);
	}

	public CaveConfiguration(File file, String configVersion)
	{
		super(file, configVersion);
	}

	public CaveConfiguration(File file, String configVersion, boolean caseSensitiveCustomCategories)
	{
		super(file, configVersion, caseSensitiveCustomCategories);
	}

	public CaveConfiguration(File file, boolean caseSensitiveCustomCategories)
	{
		super(file, caseSensitiveCustomCategories);
	}

	@Override
	public void save()
	{
		setNewCategoriesMap();

		super.save();
	}

	private void setNewCategoriesMap()
	{
		try
		{
			Field field = Configuration.class.getDeclaredField("categories");
			field.setAccessible(true);

			TreeMap<String, ConfigCategory> treeMap = (TreeMap)field.get(this);
			TreeMap<String, ConfigCategory> newMap = Maps.newTreeMap(this);
			newMap.putAll(treeMap);

			field.set(this, newMap);
		}
		catch (Throwable e) {}
	}

	@Override
	public int compare(String o1, String o2)
	{
		int result = CaveUtils.compareWithNull(o1, o2);

		if (result == 0 && o1 != null && o2 != null)
		{
			boolean flag1 = NumberUtils.isNumber(o1);
			boolean flag2 = NumberUtils.isNumber(o2);
			result = Boolean.compare(flag1, flag2);

			if (result == 0)
			{
				if (flag1 && flag2)
				{
					result = Integer.compare(NumberUtils.toInt(o1), NumberUtils.toInt(o2));
				}
				else if (!flag1 && !flag2)
				{
					result = o1.compareTo(o2);
				}
			}
		}

		return result;
	}
}