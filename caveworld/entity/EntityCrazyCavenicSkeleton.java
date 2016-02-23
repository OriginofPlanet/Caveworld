/*
 * Caveworld
 *
 * Copyright (c) 2016 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package caveworld.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import caveworld.core.CaveAchievementList;
import caveworld.entity.EntityCrazyCavenicSkeleton.Attacker;
import caveworld.item.CaveItems;
import caveworld.plugin.mceconomy.MCEconomyPlugin;
import caveworld.util.CaveUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import shift.mceconomy2.api.MCEconomyAPI;

public class EntityCrazyCavenicSkeleton extends EntityMasterCavenicSkeleton implements Comparator<Attacker>
{
	public final Map<String, Attacker> attacker = Maps.newHashMap();

	private int confusionTime = 100;
	private int confusionStart = -1;
	private int specialTime = 1000;

	public EntityCrazyCavenicSkeleton(World world)
	{
		super(world);
		this.experienceValue = 10000;
	}

	public Attacker getAttacker(EntityPlayer player)
	{
		String uuid = player.getUniqueID().toString();
		Attacker entry;

		if (attacker.containsKey(uuid))
		{
			entry = attacker.get(uuid);
		}
		else
		{
			entry = new Attacker(uuid);

			attacker.put(uuid, entry);
		}

		return entry;
	}

	public Collection<Attacker> getSortedAttacker()
	{
		List<Attacker> list = Lists.newArrayList(attacker.values());

		Collections.sort(list, this);

		return list;
	}

	public float getTotalDamage()
	{
		float total = 0.0F;

		for (Attacker entry : attacker.values())
		{
			total += entry.getDamage();
		}

		return total;
	}

	public int getAttackerOccupancy(Attacker attacker)
	{
		return MathHelper.clamp_int(MathHelper.ceiling_float_int(attacker.getDamage() / getTotalDamage() * 100), 0, 100);
	}

	@Override
	public int compare(Attacker o1, Attacker o2)
	{
		return Integer.compare(getAttackerOccupancy(o1), getAttackerOccupancy(o2));
	}

	@Override
	public IChatComponent func_145748_c_()
	{
		IChatComponent name = super.func_145748_c_();
		name.getChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(true);

		return name;
	}

	@Override
	protected void applyCustomValues()
	{
		aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 1, 2, 15.0F);

		super.applyCustomValues();
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20000.0D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(8.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.29778D);
	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();

		if (!worldObj.isRemote && getHealthScale() < 0.3F)
		{
			if (confusionTime > 0)
			{
				--confusionTime;
			}

			if (confusionTime == 0 && confusionStart == -1)
			{
				confusionStart = 500;
			}

			if (confusionStart > 0)
			{
				--confusionStart;

				List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(6.0D, 5.0D, 6.0D));
				Iterator iterator = list.iterator();

				while (iterator.hasNext())
				{
					Entity target = (Entity)iterator.next();

					if (target != null && target instanceof EntityPlayer && !target.isSprinting())
					{
						EntityArrow arrow = new EntityCavenicArrow(worldObj, target.posX + rand.nextDouble(), target.posY + target.getEyeHeight() * 3.0F + rand.nextDouble(), target.posZ + rand.nextDouble());

						arrow.shootingEntity = this;
						arrow.setDamage(3.0F + rand.nextGaussian() * 0.25D + worldObj.difficultySetting.getDifficultyId() * 0.12F);
						arrow.setThrowableHeading(0.0D, -1.0D, 0.0D, 1.0F, 1.0F);

						playSound("random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
						worldObj.spawnEntityInWorld(arrow);

						if (rand.nextInt(3) == 0)
						{
							List targets = worldObj.getEntitiesWithinAABBExcludingEntity(this, arrow.boundingBox.expand(1.5D, 1.5D, 1.5D));
							Iterator targetIterator = targets.iterator();

							while (targetIterator.hasNext())
							{
								Entity arrowTarget = (Entity)targetIterator.next();

								if (arrowTarget != null && arrowTarget instanceof EntityPlayer)
								{
									arrowTarget.attackEntityFrom(DamageSource.causeArrowDamage(arrow, this), (float)arrow.getDamage());
								}
							}
						}
					}
				}
			}

			if (confusionTime == 0 && confusionStart == 0)
			{
				confusionTime = 100;
				confusionStart = -1;
			}

			if (specialTime > 0)
			{
				--specialTime;
			}

			if (specialTime == 0)
			{
				if (rand.nextInt(2) == 0)
				{
					worldObj.newExplosion(this, posX, posY + getEyeHeight(), posZ, 1.75F + rand.nextFloat(), false, true);
				}
				else
				{
					MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
					double currentX = posX;
					double currentY = posY;
					double currentZ = posZ;
					float yaw = rotationYaw;
					float pitch = rotationPitch;

					for (Attacker entry : getSortedAttacker())
					{
						EntityPlayerMP player = server.getConfigurationManager().func_152612_a(entry.getName());

						if (player != null && player.dimension == dimension)
						{
							setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

							CaveUtils.setPlayerLocation(player, currentX, currentY, currentZ, yaw, pitch);

							break;
						}
					}
				}

				specialTime = 1000;
			}
		}
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase entity, float power)
	{
		EntityArrow arrow = new EntityCavenicArrow(worldObj, this, entity, 2.0F, 1.0F);
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, getHeldItem());
		int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, getHeldItem());
		arrow.setDamage(power * 3.0F + rand.nextGaussian() * 0.25D + worldObj.difficultySetting.getDifficultyId() * 0.12F);

		if (i > 0)
		{
			arrow.setDamage(arrow.getDamage() + i * 0.5D + 3.0D);
		}

		arrow.setKnockbackStrength(j + 5);

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, getHeldItem()) > 0)
		{
			arrow.setFire(100);
		}

		playSound("random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
		worldObj.spawnEntityInWorld(arrow);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage)
	{
		if (damage > 50.0F)
		{
			damage = 50.0F;
		}

		boolean result = super.attackEntityFrom(source, damage);

		if (result)
		{
			Entity entity = source.getEntity();

			if (entity == null)
			{
				entity = source.getSourceOfDamage();
			}

			if (entity != null && entity instanceof EntityArrow)
			{
				entity = ((EntityArrow)entity).shootingEntity;
			}

			if (entity != null && entity instanceof EntityCavenicSkeleton)
			{
				return false;
			}

			if (entity != null && entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)entity;
				Attacker entry = getAttacker(player);

				entry.setName(player.getCommandSenderName());
				entry.addDamage(damage);
			}

			float scale = getHealthScale();

			if (!worldObj.isRemote && rand.nextInt(scale < 0.2F ? 50 : scale < 0.5F ? 70 : 100) == 0)
			{
				List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(64.0D, 12.0D, 64.0D));
				Iterator iterator = list.iterator();

				while (iterator.hasNext())
				{
					Entity target = (Entity)iterator.next();

					if (target != null && (target instanceof EntityItem || target instanceof EntityLivingBase))
					{
						if (scale < 0.3F && target instanceof EntityPlayer || rand.nextInt(10) == 0)
						{
							double posX = target.posX + rand.nextDouble();
							double posY = target.posY + rand.nextDouble();
							double posZ = target.posZ + rand.nextDouble();

							if (scale < 0.75F && rand.nextInt(3) == 0)
							{
								worldObj.newExplosion(this, posX, posY, posZ, 1.5F, true, true);
							}
							else
							{
								EntityLightningBolt thunder = new EntityLightningBolt(worldObj, posX, posY, posZ);

								worldObj.addWeatherEffect(thunder);
							}
						}
					}
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public void onKillEntity(EntityLivingBase entity)
	{
		super.onKillEntity(entity);

		if (entity != null && entity instanceof EntityPlayer)
		{
			getAttacker((EntityPlayer)entity).addDeath(1);
		}
	}

	@Override
	public void onDeath(DamageSource source)
	{
		super.onDeath(source);

		Entity entity = source.getEntity();

		if (entity == null)
		{
			entity = source.getSourceOfDamage();
		}

		if (entity != null && entity instanceof EntityPlayer)
		{
			((EntityPlayer)entity).triggerAchievement(CaveAchievementList.crazyCavenicSkeletonSlayer);
		}

		if (!worldObj.isRemote)
		{
			worldObj.newExplosion(this, posX, posY + getEyeHeight(), posZ, 10.0F, false, true);

			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			List<String> names = Lists.newArrayList();
			List<IChatComponent> reports = Lists.newArrayList();

			for (Attacker entry : getSortedAttacker())
			{
				IChatComponent component = new ChatComponentTranslation("caveworld.message.crazy.report", entry.getName(), getAttackerOccupancy(entry) + "%", entry.getDeath());
				component.getChatStyle().setColor(EnumChatFormatting.GRAY);

				names.add(entry.getName());
				reports.add(component);
			}

			IChatComponent bossName = func_145748_c_();

			server.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("caveworld.message.crazy.kill", bossName));
			server.getConfigurationManager().sendChatMsg(new ChatComponentText(" ").appendSibling(new ChatComponentTranslation("caveworld.message.crazy.member", Joiner.on(", ").join(names))));
			server.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("caveworld.message.crazy.result", bossName));

			for (IChatComponent component : reports)
			{
				server.getConfigurationManager().sendChatMsg(new ChatComponentText(" ").appendSibling(component));
			}

			int i = 0;

			for (Attacker entry : getSortedAttacker())
			{
				++i;

				EntityPlayerMP player = server.getConfigurationManager().func_152612_a(entry.getName());

				if (player != null && entry.getDamage() > 0.0F)
				{
					int amount = 5000;

					if (i <= 1)
					{
						amount = 10000;
					}
					else if (i == 2)
					{
						amount = 8000;
					}

					player.addExperience(amount / 5);

					if (MCEconomyPlugin.enabled())
					{
						MCEconomyAPI.addPlayerMP(player, amount, false);
					}
				}
			}

			for (i = 0; i < 10; ++i)
			{
				entityDropItem(new ItemStack(CaveItems.cavenium, 64, 1), rand.nextFloat() + 0.1F);
			}

			entityDropItem(new ItemStack(Blocks.stonebrick, 14, 1), rand.nextFloat() + 0.1F);
		}
	}

	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);

		if (nbt.hasKey("Attacker"))
		{
			NBTTagList list = nbt.getTagList("Attacker", NBT.TAG_COMPOUND);

			attacker.clear();

			for (int i = 0; i < list.tagCount(); ++i)
			{
				NBTTagCompound data = list.getCompoundTagAt(i);
				Attacker entry = new Attacker(data);

				attacker.put(entry.getUniqueID(), entry);
			}
		}

		if (nbt.hasKey("ConfusionTime"))
		{
			confusionTime = nbt.getInteger("ConfusionTime");
		}

		if (nbt.hasKey("ConfusionStart"))
		{
			confusionStart = nbt.getInteger("ConfusionStart");
		}

		if (nbt.hasKey("SpecialTime"))
		{
			specialTime = nbt.getInteger("SpecialTime");
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);

		if (!attacker.isEmpty())
		{
			NBTTagList list = new NBTTagList();

			for (Attacker entry : attacker.values())
			{
				list.appendTag(entry.getNBTData());
			}

			nbt.setTag("Attacker", list);
		}

		nbt.setInteger("ConfusionTime", confusionTime);
		nbt.setInteger("ConfusionStart", confusionStart);
		nbt.setInteger("SpecialTime", specialTime);
	}

	public static class Attacker
	{
		private final String uuid;

		private String name;
		private float damage;
		private int death;

		public Attacker(String uuid)
		{
			this.uuid = uuid;
		}

		public Attacker(UUID uuid)
		{
			this(uuid.toString());
		}

		public Attacker(NBTTagCompound nbt)
		{
			this(nbt.getString("UUID"));
			this.name = nbt.getString("Name");
			this.damage = nbt.getFloat("Damage");
			this.death = nbt.getInteger("Death");
		}

		public String getUniqueID()
		{
			return uuid;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String str)
		{
			name = str;
		}

		public float getDamage()
		{
			return damage;
		}

		public void setDamage(float value)
		{
			damage = value;
		}

		public void addDamage(float value)
		{
			damage += value;
		}

		public int getDeath()
		{
			return death;
		}

		public void setDeath(int value)
		{
			death = value;
		}

		public void addDeath(int value)
		{
			death += value;
		}

		public NBTTagCompound getNBTData()
		{
			NBTTagCompound nbt = new NBTTagCompound();

			nbt.setString("UUID", uuid);
			nbt.setString("Name", name);
			nbt.setFloat("Damage", damage);
			nbt.setInteger("Death", death);

			return nbt;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			else if (obj == null || !(obj instanceof Attacker))
			{
				return false;
			}

			Attacker attacker = (Attacker)obj;

			return uuid.equals(attacker.uuid);
		}

		@Override
		public int hashCode()
		{
			return uuid.hashCode();
		}
	}
}