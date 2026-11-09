package com.ruletita.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RuletitaMod.MODID, value = Dist.CLIENT)
public final class ClientEventos {
    private ClientEventos() { }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var type : event.getModelTypes()) {
            Object renderer = event.getPlayerRenderer(type);
            if (renderer instanceof AvatarRenderer<?> avatar) attach(avatar);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void attach(AvatarRenderer avatar) {
        avatar.addLayer(new BellyLayer(avatar));
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null) return;
        Team team = player.getTeam();
        if (team == null) return;

        String name = team.getName();
        int separator = name.indexOf('f', 3);
        if (!name.startsWith("rzb") || separator < 0) return;

        int fat;
        try {
            fat = Math.max(0, Math.min(5, Integer.parseInt(name.substring(separator + 1))));
        } catch (NumberFormatException ignored) {
            return;
        }
        if (fat == 0) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int x = event.getWindow().getGuiScaledWidth() / 2 + 10;
        int y = event.getWindow().getGuiScaledHeight() - 47;
        for (int i = 0; i < 5; i++) {
            int left = x + i * 9;
            graphics.fill(left, y, left + 7, y + 4, 0xFF29231A);
            if (i < fat) graphics.fill(left + 1, y + 1, left + 6, y + 3, 0xFFFFD34E);
        }
    }
}
