package kegare.caveworld.proxy;

import kegare.caveworld.renderer.RenderPortalCaveworld;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		RenderPortalCaveworld.renderID = RenderingRegistry.getNextAvailableRenderId();

		RenderingRegistry.registerBlockHandler(new RenderPortalCaveworld());
	}

	@Override
	public MinecraftServer getServer()
	{
		return FMLClientHandler.instance().getServer();
	}

	@Override
	public void addChatMessage(String message)
	{
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(message);
	}
}