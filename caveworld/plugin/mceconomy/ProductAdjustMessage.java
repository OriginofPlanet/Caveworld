/*
 * Caveworld
 *
 * Copyright (c) 2016 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package caveworld.plugin.mceconomy;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

public class ProductAdjustMessage implements IMessage, IMessageHandler<ProductAdjustMessage, IMessage>
{
	private int type;
	private NBTTagCompound data;

	public ProductAdjustMessage() {}

	public ProductAdjustMessage(IShopProductManager manager)
	{
		this.type = manager.getType();
		this.data = new NBTTagCompound();
		this.data.setTag("Entries", manager.saveNBTData());
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		type = buffer.readInt();
		data = ByteBufUtils.readTag(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeInt(type);
		ByteBufUtils.writeTag(buffer, data);
	}

	@Override
	public IMessage onMessage(ProductAdjustMessage message, MessageContext ctx)
	{
		IShopProductManager manager;

		switch (message.type)
		{
			default:
				manager = MCEconomyPlugin.productManager;
				break;
		}

		manager.clearProducts();
		manager.loadNBTData(message.data.getTagList("Entries", NBT.TAG_COMPOUND));
		manager.setReadOnly(true);

		return null;
	}
}