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
    private static int cleanupTicks;
    private static int debugTicks = 100;

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
        if (++debugTicks >= 100) {
            debugTicks = 0;
            Debug.reload(minecraft.gameDirectory);
        }
        if (!Debug.clientTick) return;
        ClientLevel level = minecraft.level;
        if (level != cachedLevel) {
            RENDER_STATES.clear();
            cachedLevel = level;
        }
        if (level == null) return;

        for (Player player : level.players()) {
            Team team = player.getTeam();
            RenderState state = RENDER_STATES.get(player.getId());
            if (state == null) {
                state = new RenderState();
                RENDER_STATES.put(player.getId(), state);
            }
            // Solo se vuelve a interpretar el nombre del equipo cuando cambia: antes se
            // analizaba una cadena por jugador en cada tick.
            if (team != state.lastTeam) {
                state.lastTeam = team;
                state.fat = parseField(team, 'f', 7);
                state.muscle = parseField(team, 'm', 3);
                state.progress = parseField(team, 'p', 9);
                state.timer = parseField(team, 't', 9);
            }
            state.targetFat = state.fat;
        }

        if (++cleanupTicks >= 40) {
            cleanupTicks = 0;
            RENDER_STATES.keySet().removeIf(entityId -> level.getEntity(entityId) == null);
        }
    }

    /** Campo numérico del nombre del equipo: rzb{nivel}f{grasa}m{músculo}p{progreso}t{temporizador}. */
    private static int parseField(Team team, char key, int max) {
        if (team == null) return 0;
        String name = team.getName();
        if (!name.startsWith("rzb")) return 0;
        int start = name.indexOf(key, 3);
        if (start < 0) return 0;
        int end = start + 1;
        while (end < name.length() && Character.isDigit(name.charAt(end))) end++;
        if (end == start + 1) return 0;
        try {
            return Math.max(0, Math.min(max, Integer.parseInt(name.substring(start + 1, end))));
        } catch (NumberFormatException ignored) {
            return 0;
        }
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
     * Reserva adicional: siete segmentos independientes, alineados debajo de la barra
     * nativa de hambre. Se dibuja con primitivas GUI ya disponibles, sin texturas ni ticks extra.
     */
    // Copia píxel por píxel del muslo de hambre de vanilla (9x9), recoloreada en dorado.
    // Igual que vanilla: primero el contorno vacío y, si hay grasa, el relleno encima.
    private static final String[] EMPTY_GRID = {
            "..oo.....",
            ".oppo....",
            "oppppo...",
            "opppppo..",
            ".oppppo..",
            "..opppo..",
            "...ooopoo",
            "......opo",
            "......oo."
    };
    private static final String[] FULL_GRID = {
            ".........",
            "..ab.....",
            ".acad....",
            ".baded...",
            "..fgee...",
            "...fgg...",
            "......h..",
            ".......i.",
            "........."
    };

    private static void renderFatReserve(GuiGraphics graphics, DeltaTracker delta) {
        if (!Debug.hud) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.gameMode == null
                || !minecraft.gameMode.canHurtPlayer()) return;

        RenderState state = RENDER_STATES.get(player.getId());
        int fat = state == null ? 0 : state.fat;
        int muscle = state == null ? 0 : state.muscle;
        int progress = state == null ? 0 : state.progress;
        int timer = state == null ? 0 : state.timer;

        // Fila de grasa justo sobre la barra de hambre. Bajo el agua, las burbujas ocupan esa fila:
        // la grasa sube una fila más para que nunca se mezclen.
        boolean bubbles = player.isUnderWater() || player.getAirSupply() < player.getMaxAirSupply();
        int y = graphics.guiHeight() - 49 - (bubbles ? 10 : 0);
        int right = graphics.guiWidth() / 2 + 82;
        for (int i = 0; i < 7; i++) {
            // Los primeros 4 son la zona útil (dorados); los últimos 3, panza pura (grasosos).
            boolean full = i < fat;
            // La grasa que está por bajar se vacía de abajo hacia arriba y titila al final.
            int cutoff = (full && i == fat - 1) ? 1 + timer * 7 / 10 : 0;
            if (full && i == fat - 1 && timer >= 8 && (System.currentTimeMillis() / 500L) % 2L == 0L) cutoff = 9;
            drawFatIcon(graphics, right - i * 8, y, full, i >= 4, cutoff);
        }

        // Músculo: 3 bíceps sobre la barra de corazones (y la armadura). El próximo se llena con el
        // entrenamiento; si no hay grasa útil queda apagado, porque no hay combustible.
        int left = graphics.guiWidth() / 2 - 91;
        // Mismo cálculo que vanilla para las filas de corazones (vida extra / absorción) y la armadura.
        int heartRows = (int) Math.ceil((player.getMaxHealth() + Math.ceil(player.getAbsorptionAmount())) / 20.0);
        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int my = graphics.guiHeight() - 49 - (heartRows - 1) * rowHeight - (player.getArmorValue() > 0 ? 10 : 0);
        for (int i = 0; i < 3; i++) {
            drawRects(graphics, BICEP_EMPTY_RECTS, left + i * 8, my, 0, 0);
            if (i < muscle) {
                drawRects(graphics, BICEP_FULL_RECTS, left + i * 8, my, 2, 0);
            } else if (i == muscle) {
                drawRects(graphics, BICEP_FULL_RECTS, left + i * 8, my, fat > 0 ? 2 : 3, 8 - (progress + 1) * 7 / 10);
            }
        }
    }

    /** Rectángulo precalculado del icono: posición relativa, tamaño y carácter de color. */
    private record Rect(int x, int y, int w, int h, char c) { }

    private static final Rect[] EMPTY_RECTS = buildRects(EMPTY_GRID);
    private static final Rect[] FULL_RECTS = buildRects(FULL_GRID);

    /** Agrupa píxeles contiguos del mismo color en pocos rectángulos. */
    private static Rect[] buildRects(String[] grid) {
        java.util.List<int[]> open = new java.util.ArrayList<>();      // {x, y, w, h, c}
        java.util.List<Rect> done = new java.util.ArrayList<>();
        for (int row = 0; row < grid.length; row++) {
            String line = grid[row];
            java.util.List<int[]> next = new java.util.ArrayList<>();
            int col = 0;
            while (col < line.length()) {
                char c = line.charAt(col);
                if (c == '.') { col++; continue; }
                int end = col;
                while (end < line.length() && line.charAt(end) == c) end++;
                int w = end - col;
                int[] match = null;
                for (int[] r : open) {
                    if (r[0] == col && r[2] == w && r[4] == c && r[1] + r[3] == row) { match = r; break; }
                }
                if (match != null) { match[3]++; open.remove(match); next.add(match); }
                else next.add(new int[]{col, row, w, 1, c});
                col = end;
            }
            for (int[] r : open) done.add(new Rect(r[0], r[1], r[2], r[3], (char) r[4]));
            open = next;
        }
        for (int[] r : open) done.add(new Rect(r[0], r[1], r[2], r[3], (char) r[4]));
        return done.toArray(new Rect[0]);
    }

    // Variante "grasosa" de la zona pura: marrón amarillento, con brillo.
    private static int greasyColorOf(char c) {
        return switch (c) {
            case 'o' -> 0xFF000000;
            case 'p' -> 0xFF282828;
            case 'a' -> 0xFF8A6A10;
            case 'b' -> 0xFF6E5208;
            case 'c' -> 0xFFC9A63A;
            case 'd' -> 0xFFA8832A;
            case 'e' -> 0xFF957022;
            case 'f' -> 0xFF5C4204;
            case 'g' -> 0xFF80600F;
            case 'h' -> 0xFFD9BC5A;
            default -> 0xFFE6CE72;
        };
    }

    private static int colorOf(char c) {
        return switch (c) {
            case 'o' -> 0xFF000000;   // contorno (igual que vanilla)
            case 'p' -> 0xFF282828;   // fondo vacío (igual que vanilla)
            case 'a' -> 0xFFD09522;
            case 'b' -> 0xFFC68006;
            case 'c' -> 0xFFEFD374;
            case 'd' -> 0xFFE1B850;
            case 'e' -> 0xFFDAA83B;
            case 'f' -> 0xFFC47C00;
            case 'g' -> 0xFFCF931E;
            case 'h' -> 0xFFF6E187;
            default -> 0xFFFFF4A0;    // 'i': punta del hueso
        };
    }

    // Bíceps (9x9): contorno y relleno, mismo sistema de rectángulos que el muslo.
    private static final String[] BICEP_EMPTY = {
            "......oo.",
            ".....opo.",
            ".oo.oppo.",
            "opppoppo.",
            "oppppppo.",
            ".opppppo.",
            "..oppppo.",
            "...opppo.",
            "....ooo.."
    };
    private static final String[] BICEP_FULL = {
            ".........",
            "......a..",
            ".....ab..",
            ".cad.ab..",
            ".caadab..",
            "..caabb..",
            "...aabb..",
            "....abb..",
            "........."
    };
    private static final Rect[] BICEP_EMPTY_RECTS = buildRects(BICEP_EMPTY);
    private static final Rect[] BICEP_FULL_RECTS = buildRects(BICEP_FULL);

    private static int bicepColorOf(char c, boolean dim) {
        return switch (c) {
            case 'o' -> 0xFF000000;
            case 'p' -> 0xFF282828;
            case 'a' -> dim ? 0xFF5A5A5A : 0xFFC8442E;
            case 'b' -> dim ? 0xFF484848 : 0xFFA83320;
            case 'c' -> dim ? 0xFF787878 : 0xFFE8806A;
            default -> dim ? 0xFF666666 : 0xFFD65A40;
        };
    }

    /** mode: 0 = dorado, 1 = grasoso, 2 = bíceps, 3 = bíceps apagado. Solo se dibujan las filas >= fromRow. */
    private static void drawRects(GuiGraphics graphics, Rect[] rects, int x, int y, int mode, int fromRow) {
        for (Rect r : rects) {
            int top = Math.max(r.y(), fromRow);
            int bottom = r.y() + r.h();
            if (top >= bottom) continue;
            int color = switch (mode) {
                case 1 -> greasyColorOf(r.c());
                case 2 -> bicepColorOf(r.c(), false);
                case 3 -> bicepColorOf(r.c(), true);
                default -> colorOf(r.c());
            };
            graphics.fill(x + r.x(), y + top, x + r.x() + r.w(), y + bottom, color);
        }
    }

    private static void drawFatIcon(GuiGraphics graphics, int x, int y, boolean full, boolean greasy, int fromRow) {
        drawRects(graphics, EMPTY_RECTS, x, y, 0, 0);
        if (full) drawRects(graphics, FULL_RECTS, x, y, greasy ? 1 : 0, fromRow);
    }

    private static final class RenderState {
        private float currentFat;
        private int targetFat;
        private int fat;
        private int muscle;
        private int progress;
        private int timer;
        private long lastNanos;
        private Team lastTeam;

        private RenderState() {
            this.currentFat = 0f;
            this.targetFat = 0;
        }
    }
}
