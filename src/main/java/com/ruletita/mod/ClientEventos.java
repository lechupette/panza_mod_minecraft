package com.ruletita.mod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = RuletitaMod.MODID, value = Dist.CLIENT)
public final class ClientEventos {
    private static final Map<Integer, RenderState> RENDER_STATES = new HashMap<>();
    private static ClientLevel cachedLevel;

    private ClientEventos() { }

    static {
        AddGuiOverlayLayersEvent.BUS.addListener(ClientEventos::addHudLayer);
        TickEvent.ClientTickEvent.Post.BUS.addListener(ClientEventos::refreshRenderStates);
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

    /**
     * Lee los equipos una vez por tick de cliente. El render de cada fotograma solo consulta
     * una entrada ya calculada: no busca entidades ni analiza cadenas durante el render.
     */
    private static void refreshRenderStates(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != cachedLevel) {
            RENDER_STATES.clear();
            cachedLevel = level;
        }
        if (level == null) return;

        for (Player player : level.players()) {
            int fat = 0;
            Team team = player.getTeam();
            if (team != null) {
                String name = team.getName();
                if (name.startsWith("rzb")) {
                    int separator = name.indexOf('f', 3);
                    try {
                        if (separator >= 0) {
                            fat = Math.max(0, Math.min(5, Integer.parseInt(name.substring(separator + 1))));
                        }
                    } catch (NumberFormatException ignored) {
                        fat = 0;
                    }
                }
            }

            RenderState state = RENDER_STATES.get(player.getId());
            if (state == null) {
                state = new RenderState();
                RENDER_STATES.put(player.getId(), state);
            }
            state.targetFat = fat;
            state.fat = fat;
        }

        RENDER_STATES.keySet().removeIf(entityId -> level.getEntity(entityId) == null);
    }

    static float smoothFat(int entityId) {
        RenderState state = RENDER_STATES.get(entityId);
        if (state == null) return 0f;

        long now = System.nanoTime();
        if (state.lastNanos == 0L) state.lastNanos = now;
        float dt = (float) Math.min(0.1, (now - state.lastNanos) / 1.0e9);
        state.currentFat += (state.targetFat - state.currentFat) * Math.min(1f, dt * 6f);
        state.lastNanos = now;
        return state.currentFat;
    }

    private static void addHudLayer(AddGuiOverlayLayersEvent event) {
        Identifier name = Identifier.fromNamespaceAndPath(RuletitaMod.MODID, "fat_reserve");
        event.getLayeredDraw().add(ForgeLayeredDraw.PRE_SLEEP_STACK, name, ClientEventos::renderFatReserve);
    }

    /**
     * Reserva adicional: cinco segmentos independientes, alineados debajo de la barra
     * nativa de hambre. Se dibuja con primitivas GUI ya disponibles, sin texturas ni ticks extra.
     */
    private static void renderFatReserve(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.gameMode == null
                || !minecraft.gameMode.canHurtPlayer()) return;

        RenderState state = RENDER_STATES.get(player.getId());
        int fat = state == null ? 0 : state.fat;

        // Sobrebarra: justo encima de la barra de hambre nativa (que ocupa x+10..x+91, y = alto-39).
        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() - 39 - 7;
        for (int i = 0; i < 5; i++) {
            int left = x + i * 16;
            boolean full = i < fat;
            graphics.fill(left, y, left + 15, y + 5, 0xFF2B2415);
            graphics.fill(left + 1, y + 1, left + 14, y + 4, full ? 0xFFE6B422 : 0xFF4A4332);
            if (full) graphics.fill(left + 1, y + 1, left + 14, y + 2, 0xFFFBE27A);
        }
    }

    private static final class RenderState {
        private float currentFat;
        private int targetFat;
        private int fat;
        private long lastNanos;

        private RenderState() {
            this.currentFat = 0f;
            this.targetFat = 0;
        }
    }
}
