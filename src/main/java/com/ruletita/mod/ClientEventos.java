package com.ruletita.mod;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RuletitaMod.MODID, value = Dist.CLIENT)
public final class ClientEventos {
    private ClientEventos() { }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var type : event.getModelTypes()) {
            Object renderer = event.getPlayerRenderer(type);
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
