package com.ruletita.panza;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PanzaMod.MODID)
public class PanzaMod {
    public static final String MODID = "ruletita_panza";

    public PanzaMod(FMLJavaModLoadingContext context) {
        EntityRenderersEvent.AddLayers.getBus(context.getModBusGroup()).addListener(PanzaMod::onAddLayers);
    }

    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            Object renderer = event.getSkin(skin);
            if (renderer instanceof AvatarRenderer<?> avatar) {
                attach(avatar);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void attach(AvatarRenderer avatar) {
        avatar.addLayer(new BellyLayer(avatar));
    }
}
