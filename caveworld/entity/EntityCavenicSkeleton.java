/*
 * Caveworld
 *
 * Copyright (c) 2016 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package caveworld.entity;

import org.apache.commons.lang3.ArrayUtils;

import caveworld.api.CaveworldAPI;
import caveworld.core.CaveAchievementList;
import caveworld.item.CaveItems;
import caveworld.util.CaveUtils;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class EntityCavenicSkeleton extends EntitySkeleton
{
	public static int spawnWeight;
	public static int spawnMinHeight;
	public static int spawnMaxHeight;
	public static int spawnInChunks;
	public static int[] spawnBiomes;

	public static void refreshSpawn()
	{
		BiomeGenBase[] def = CaveUtils.getBiomes().toArray(new BiomeGenBase[0]);
		BiomeGenBase[] biomes = new BiomeGenBase[0];
		BiomeGenBase biome;

		for (int i : spawnBiomes)
		{
			if (i >= 0 && i < BiomeGenBase.getBiomeGenArray().length)
			{
				biome = BiomeGenBase.getBiome(i);

				if (biome != null)
				{
					biomes = ArrayUtils.add(biomes, biome);
				}
			}
		}

		if (ArrayUtils.isEmpty(biomes))
		{
			biomes = def;
		}

		EntityRegistry.removeSpawn(EntityCavenicSkeleton.class, EnumCreatureType.monster, def);

		if (spawnWeight > 0)
		{
			EntityRegistry.addSpawn(EntityCavenicSkeleton.class, spawnWeight, 4, 4, EnumCreatureType.monster, biomes);
		}
	}

	protected EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 0.975D, 1, 3, 8.0F);

	public EntityCavenicSkeleton(World world)
	{
		super(world);
		this.experienceValue = 10;
		this.setSize(0.68F, 2.0F);

		ObfuscationReflectionHelper.setPrivateValue(EntitySkeleton.class, this, aiArrowAttack, "aiArrowAttack", "field_85037_d");
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50.0D + 10.0D * rand.nextInt(3));
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(2.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data)
	{
		if (!worldObj.isRemote && rand.nextInt(100) == 0)
		{
			EntityMasterCavenicSkeleton master = new EntityMasterCavenicSkeleton(worldObj);
			master.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);

			worldObj.spawnEntityInWorld(master);
			setDead();

			return data;
		}

		tasks.addTask(4, aiArrowAttack);
		addRandomArmor();
		enchantEquipment();

		return data;
	}

	@Override
	public void onStruckByLightning(EntityLightningBolt thunder)
	{
		if (!worldObj.isRemote && rand.nextInt(3) == 0)
		{
			EntityMasterCavenicSkeleton master = new EntityMasterCavenicSkeleton(worldObj);
			master.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);

			worldObj.spawnEntityInWorld(master);
			setDead();
		}

		super.onStruckByLightning(thunder);
	}

	@Override
	protected void addRandomArmor()
	{
		super.addRandomArmor();

		if (rand.nextInt(15) == 0)
		{
			setCurrentItemOrArmor(0, new ItemStack(CaveItems.cavenic_bow));
			setEquipmentDropChance(0, 2.0F);
		}
		else
		{
			setCurrentItemOrArmor(0, new ItemStack(Items.bow));
		}
	}

	@Override
	public void setCombatTask()
	{
		tasks.removeTask(aiArrowAttack);

		ItemStack itemstack = getHeldItem();

		if (itemstack != null && (itemstack.getItem() == Items.bow || itemstack.getItem() == CaveItems.cavenic_bow))
		{
			tasks.addTask(4, aiArrowAttack);
		}
	}

	@Override
	public int getSkeletonType()
	{
		return 0;
	}

	@Override
	public void setSkeletonType(int type)
	{
		setSize(0.68F, 2.0F);
	}

	@Override
	protected void dropFewItems(boolean par1, int looting)
	{
		super.dropFewItems(par1, looting);

		entityDropItem(new ItemStack(CaveItems.cavenium, 1, rand.nextInt(2)), 0.5F);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage)
	{
		return !source.isFireDamage() && super.attackEntityFrom(source, damage);
	}

	@Override
	protected void fall(float damage)
	{
		PotionEffect potion = getActivePotionEffect(Potion.jump);
		float f1 = potion != null ? (float)(potion.getAmplifier() + 1) : 0.0F;
		int i = MathHelper.ceiling_float_int(damage - 3.0F - f1);

		if (i > 0)
		{
			playSound(func_146067_o(i), 1.0F, 1.0F);

			int x = MathHelper.floor_double(posX);
			int y = MathHelper.floor_double(posY - 0.20000000298023224D - yOffset);
			int z = MathHelper.floor_double(posZ);
			Block block = worldObj.getBlock(x, y, z);

			if (block.getMaterial() != Material.air)
			{
				playSound(block.stepSound.getStepResourcePath(), block.stepSound.getVolume() * 0.5F, block.stepSound.getPitch() * 0.75F);
			}
		}
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase entity, float power)
	{
		EntityArrow arrow = new EntityCavenicArrow(worldObj, this, entity, 1.6F, 14 - worldObj.difficultySetting.getDifficultyId() * 4);
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, getHeldItem());
		int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, getHeldItem());
		arrow.setDamage(power * 2.0F + rand.nextGaussian() * 0.25D + worldObj.difficultySetting.getDifficultyId() * 0.11F);

		if (i > 0)
		{
			arrow.setDamage(arrow.getDamage() + i * 0.5D + 0.5D);
		}

		if (j > 0)
		{
			arrow.setKnockbackStrength(j);
		}

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, getHeldItem()) > 0 || getSkeletonType() == 1)
		{
			arrow.setFire(100);
		}

		playSound("random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
		worldObj.spawnEntityInWorld(arrow);
	}

	@Override
	public void onDeath(DamageSource source)
	{
		super.onDeath(source);

		Entity entity = source.getEntity();

		if (entity != null && entity instanceof EntityPlayer)
		{
			((EntityPlayer)entity).triggerAchievement(CaveAchievementList.cavenicSkeletonSlayer);
		}
	}

	@Override
	public boolean getCanSpawnHere()
	{
		int y = MathHelper.floor_double(boundingBox.minY);

		return CaveworldAPI.isEntityInCaves(this) && y >= spawnMinHeight && y <= spawnMaxHeight && super.getCanSpawnHere();
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return spawnInChunks;
	}
}