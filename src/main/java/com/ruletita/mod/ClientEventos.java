package com.ruletita.mod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RuletitaMod.MODID, value = Dist.CLIENT)
public final class ClientEventos {
    private ClientEventos() { }

    static {
        // Forge 1.21.11 inserta HUDs personalizados mediante ForgeLayeredDraw.
        AddGuiOverlayLayersEvent.BUS.addListener(ClientEventos::addHudLayer);
    }

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

    private static void addHudLayer(AddGuiOverlayLayersEvent event) {
        Identifier name = Identifier.fromNamespaceAndPath(RuletitaMod.MODID, "fat_reserve");
        event.getLayeredDraw().addAbove(
                ForgeLayeredDraw.HOTBAR_AND_DECOS,
                name,
                ForgeLayeredDraw.HEALTH_BAR,
                ClientEventos::renderFatReserve
        );
    }

    private static void renderFatReserve(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null || minecraft.gameMode == null || minecraft.gameMode.isSpectator()) return;

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

        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() - 47;
        for (int i = 0; i < 5; i++) {
            int left = x + i * 9;
            graphics.fill(left, y, left + 7, y + 4, 0xFF29231A);
            if (i < fat) graphics.fill(left + 1, y + 1, left + 6, y + 3, 0xFFFFD34E);
        }
    }
}
