package com.ruletita.mod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.Stats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

/** Logica de servidor: alimentacion, grasa, cafe, insomnio y comandos de prueba. */
public final class Eventos {
    private static final int MAX_GRASA = 7;
    // Grasa 1..4 = util (alimenta el musculo); 5..7 = panza pura.
    private static final int GRASA_UTIL = 4;
    // Cada cuantos ticks baja 1 nivel de grasa (15 min en zona util, mas lento en zona pura).
    private static final int DECAY_UTIL = 18000;
    private static final int DECAY_PURA = 30000;
    // Musculo: niveles y puntos de entrenamiento por nivel (~30 min de juego activo).
    private static final int MAX_MUSCULO = 3;
    private static final int PUNTOS_NIVEL = 9000;
    // Tope de puntos por minuto y por fuente, para que no se pueda farmear una sola cosa.
    private static final int POLLS_VENTANA = 60;
    private static final int TOPE_CORRER = 400, TOPE_MINAR = 360, TOPE_PELEAR = 300, TOPE_SALTAR = 120;
    private static final Identifier MOD_VELOCIDAD = Identifier.fromNamespaceAndPath(RuletitaMod.MODID, "grasa");

    private Eventos() { }

    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("panza")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("hambre")
                        .then(Commands.argument("puntos", IntegerArgumentType.integer(1, 20))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int points = IntegerArgumentType.getInteger(context, "puntos");
                                    player.getFoodData().eat(points, 0.0F);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Hambre aumentada en " + points + " puntos."),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("grasa")
                        .then(Commands.argument("niveles", IntegerArgumentType.integer(1, 7))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "niveles");
                                    CompoundTag data = player.getPersistentData();
                                    int before = Math.max(0, data.getIntOr("rz_grasa", 0));
                                    int after = Math.min(MAX_GRASA, before + amount);
                                    data.putInt("rz_grasa", after);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Grasa: " + after + " / " + MAX_GRASA + "."),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("musculo")
                        .then(Commands.argument("nivel", IntegerArgumentType.integer(0, MAX_MUSCULO))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int nivel = IntegerArgumentType.getInteger(context, "nivel");
                                    player.getPersistentData().putInt("rz_musc", nivel);
                                    player.getPersistentData().putInt("rz_mpts", 0);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Musculo: " + nivel + " / " + MAX_MUSCULO + "."), true);
                                    return 1;
                                })))
                .then(Commands.literal("estado")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            CompoundTag data = player.getPersistentData();
                            String texto = "Nivel visual: " + data.getIntOr("rz_nivel", 0) + " / " + (10 + MAX_GRASA)
                                    + " · Grasa: " + data.getIntOr("rz_grasa", 0) + " / " + MAX_GRASA
                                    + " · Musculo: " + data.getIntOr("rz_musc", 0) + " / " + MAX_MUSCULO
                                    + " (progreso " + data.getIntOr("rz_mpts", 0) + " / " + PUNTOS_NIVEL + ")";
                            context.getSource().sendSuccess(() -> Component.literal(texto), false);
                            return 1;
                        }))
                .then(Commands.literal("limpiar_grasa")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.getPersistentData().putInt("rz_grasa", 0);
                            context.getSource().sendSuccess(() -> Component.literal("Grasa restablecida."), true);
                            return 1;
                        })));
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (!Debug.serverTick) return;
        if (!(event.player() instanceof ServerPlayer sp)) return;
        CompoundTag d = sp.getPersistentData();
        int food = sp.getFoodData().getFoodLevel();
        int storedFat = d.getIntOr("rz_grasa", 0);
        int grasa = Math.max(0, Math.min(MAX_GRASA, storedFat));
        if (grasa != storedFat) d.putInt("rz_grasa", grasa);

        int cd = d.getIntOr("rz_cd", 0);
        if (cd <= 0) cd = 600 + sp.getRandom().nextInt(1801);
        cd -= (food >= 16 ? 2 : 1);
        if (cd <= 0) {
            flatulencia(sp);
            cd = 600 + sp.getRandom().nextInt(1801);
        }
        d.putInt("rz_cd", cd);

        int dec = d.getIntOr("rz_dec", 0) + 1;
        if (dec >= (grasa > GRASA_UTIL ? DECAY_PURA : DECAY_UTIL)) {
            dec = 0;
            if (grasa > 0) {
                grasa--;
                d.putInt("rz_grasa", grasa);
            }
        }
        d.putInt("rz_dec", dec);

        grasa = entrenar(sp, d, grasa);
        int musculo = d.getIntOr("rz_musc", 0);
        int progreso = d.getIntOr("rz_mpts", 0) * 10 / PUNTOS_NIVEL;
        int decayLimit = grasa > GRASA_UTIL ? DECAY_PURA : DECAY_UTIL;
        int temporizador = grasa > 0 ? Math.min(9, dec * 10 / decayLimit) : 0;

        if (sp.level().getDayTime() % 24000L < 12000L && d.getBooleanOr("rz_insomnio", false)) {
            d.putBoolean("rz_insomnio", false);
        }

        // Comer con la barra llena: vanilla no lo permite. Mientras se sostiene comida con hambre 20,
        // se baja un punto el nivel para habilitar el consumo y se marca para contarlo como exceso.
        boolean holdingFood = esComidaNormal(sp.getMainHandItem()) || esComidaNormal(sp.getOffhandItem());
        boolean lleno = d.getBooleanOr("rz_lleno", false);
        if (!lleno && holdingFood && food >= 20 && !sp.isCreative() && !sp.isSpectator()) {
            sp.getFoodData().setFoodLevel(19);
            d.putBoolean("rz_lleno", true);
            food = 19;
        } else if (lleno && !sp.isUsingItem() && !holdingFood) {
            if (sp.getFoodData().getFoodLevel() == 19) sp.getFoodData().setFoodLevel(20);
            d.putBoolean("rz_lleno", false);
            food = sp.getFoodData().getFoodLevel();
        }

        // Diez niveles de hambre nativa más siete niveles independientes de reserva (4 útiles + 3 de panza pura).
        int alimentacion = (d.getBooleanOr("rz_lleno", false) ? 20 : food) / 2;
        int nivel = Math.max(0, Math.min(10, alimentacion)) + grasa;
        // El nombre del equipo lleva todo el estado visible hacia el cliente (max. 16 caracteres).
        String teamName = "rzb" + nivel + "f" + grasa + "m" + musculo + "p" + Math.min(9, progreso) + "t" + temporizador;
        if (!teamName.equals(d.getStringOr("rz_team", ""))) {
            d.putString("rz_team", teamName);
            d.putInt("rz_nivel", nivel);
            asignarEquipo(sp, teamName);
        }

        if (grasa != d.getIntOr("rz_vel", -1)) {
            d.putInt("rz_vel", grasa);
            AttributeInstance inst = sp.getAttribute(Attributes.MOVEMENT_SPEED);
            if (inst != null) {
                inst.removeModifier(MOD_VELOCIDAD);
                if (grasa > 0) {
                    inst.addPermanentModifier(new AttributeModifier(MOD_VELOCIDAD, -0.25 * grasa / MAX_GRASA, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
        }
    }

    private static void flatulencia(ServerPlayer sp) {
        ServerLevel level = (ServerLevel) sp.level();
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), RuletitaMod.FART.get(), SoundSource.PLAYERS,
                1.0f, 0.9f + sp.getRandom().nextFloat() * 0.3f);
        level.sendParticles(ParticleTypes.POOF, sp.getX(), sp.getY() + 0.6, sp.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
        // Nube (POOF) + tres colores a la vez: verde oliva, marrón y amarillo mostaza.
        level.sendParticles(new DustParticleOptions(0x6B7B2E, 1.4f), sp.getX(), sp.getY() + 0.6, sp.getZ(), 5, 0.25, 0.12, 0.25, 0.01);
        level.sendParticles(new DustParticleOptions(0x6A4A25, 1.4f), sp.getX(), sp.getY() + 0.6, sp.getZ(), 5, 0.25, 0.12, 0.25, 0.01);
        level.sendParticles(new DustParticleOptions(0xC9A227, 1.4f), sp.getX(), sp.getY() + 0.6, sp.getZ(), 5, 0.25, 0.12, 0.25, 0.01);
    }

    /** Diferencia con la lectura anterior de una estadistica; 0 la primera vez o si se reinició. */
    private static int delta(CompoundTag d, String key, int current) {
        int last = d.getIntOr(key, -1);
        d.putInt(key, current);
        if (last < 0 || current < last) return 0;
        return current - last;
    }

    /** Suma puntos a una fuente sin pasar su tope de la ventana actual. */
    private static int acumular(CompoundTag d, String key, int puntos, int tope) {
        int usado = d.getIntOr(key, 0);
        int ok = Math.max(0, Math.min(puntos, tope - usado));
        d.putInt(key, usado + ok);
        return ok;
    }

    /**
     * Entrenamiento por actividad normal (correr, minar, pegar, saltar), leido de las
     * estadisticas del jugador una vez por segundo. Convierte grasa util en musculo.
     * Devuelve la grasa resultante.
     */
    private static int entrenar(ServerPlayer sp, CompoundTag d, int grasa) {
        int musculo = Math.max(0, Math.min(MAX_MUSCULO, d.getIntOr("rz_musc", 0)));
        if (musculo != d.getIntOr("rz_musc", 0)) d.putInt("rz_musc", musculo);
        if (sp.tickCount % 20 != 0) return grasa;

        var stats = sp.getStats();
        int sprint = delta(d, "rz_st_sprint", stats.getValue(Stats.CUSTOM, Stats.SPRINT_ONE_CM));
        int walk = delta(d, "rz_st_walk", stats.getValue(Stats.CUSTOM, Stats.WALK_ONE_CM));
        int jumps = delta(d, "rz_st_jump", stats.getValue(Stats.CUSTOM, Stats.JUMP));
        int damage = delta(d, "rz_st_dmg", stats.getValue(Stats.CUSTOM, Stats.DAMAGE_DEALT));
        int mined = 0;
        for (var block : BuiltInRegistries.BLOCK) mined += stats.getValue(Stats.BLOCK_MINED, block);
        mined = delta(d, "rz_st_mined", mined);

        int ventana = d.getIntOr("rz_cap_t", 0) + 1;
        if (ventana >= POLLS_VENTANA) {
            ventana = 0;
            d.putInt("rz_cap_run", 0);
            d.putInt("rz_cap_mine", 0);
            d.putInt("rz_cap_hit", 0);
            d.putInt("rz_cap_jump", 0);
        }
        d.putInt("rz_cap_t", ventana);

        if (musculo >= MAX_MUSCULO) return grasa;
        int puntos = 0;
        puntos += acumular(d, "rz_cap_run", sprint / 100, TOPE_CORRER);
        puntos += acumular(d, "rz_cap_mine", mined * 6, TOPE_MINAR);
        puntos += acumular(d, "rz_cap_hit", damage * 3 / 10, TOPE_PELEAR);
        // Saltar solo cuenta si ademas te moves: saltar en el lugar no suma.
        if (sprint + walk >= 100) puntos += acumular(d, "rz_cap_jump", jumps * 2, TOPE_SALTAR);

        // Sin grasa util no hay combustible; con mas grasa util rinde mas (40 %..100 %).
        int util = Math.min(grasa, GRASA_UTIL);
        if (util <= 0 || puntos <= 0) return grasa;
        int pts = d.getIntOr("rz_mpts", 0) + puntos * (2 + 2 * util) / 10;
        if (pts >= PUNTOS_NIVEL) {
            pts = 0;
            musculo++;
            grasa--;
            d.putInt("rz_musc", musculo);
            d.putInt("rz_grasa", grasa);
            sp.displayClientMessage(Component.literal("Tus musculos te agradecen la grasa"), true);
        }
        d.putInt("rz_mpts", pts);
        return grasa;
    }

    private static void asignarEquipo(ServerPlayer sp, String name) {
        MinecraftServer server = sp.level().getServer();
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) team = scoreboard.addPlayerTeam(name);
        scoreboard.addPlayerToTeam(sp.getScoreboardName(), team);
    }

    /**
     * Permite empezar a comer cualquier alimento con la barra de hambre llena (vanilla lo impide).
     * Solo en servidor; los alimentos "siempre comestibles" siguen el camino normal.
     */
    /** El aldeano granjero vende cerezas de café en el nivel de novato. */
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (!event.getType().toString().contains("farmer")) return;
        var level1 = event.getTrades().get(1);
        if (level1 == null) return;
        level1.add((level, trader, random) -> new MerchantOffer(
                new ItemCost(net.minecraft.world.item.Items.EMERALD, 1),
                new ItemStack(RuletitaMod.CEREZAS_CAFE.get(), 4),
                12, 2, 0.05f));
    }

    private static boolean esComidaNormal(ItemStack stack) {
        var food = stack.get(DataComponents.FOOD);
        return food != null && !food.canAlwaysEat() && stack.get(DataComponents.CONSUMABLE) != null;
    }

    private static boolean esKuadro(ItemStack stack) {
        if (!stack.is(net.minecraft.world.item.Items.PAINTING)) return false;
        var custom = stack.get(DataComponents.CUSTOM_DATA);
        return custom != null && custom.copyTag().contains("kuadro");
    }

    public static boolean onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return false;
        ItemStack stack = event.getItemStack();
        // Kuadro en la mano + clic derecho al aire: abre el selector de medida (sin escribir comandos).
        if (esKuadro(stack) && sp.level().getServer() != null) {
            var server = sp.level().getServer();
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withEntity(sp).withPosition(sp.position()),
                    "function kuadro:menu");
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }
        var food = stack.get(DataComponents.FOOD);
        if (food == null || stack.get(DataComponents.CONSUMABLE) == null) return false;
        if (food.canAlwaysEat() || sp.isUsingItem() || sp.getFoodData().getFoodLevel() < 20) return false;
        sp.startUsingItem(event.getHand());
        event.setCancellationResult(InteractionResult.CONSUME);
        return true;
    }

    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer sp && event.getItem().get(DataComponents.FOOD) != null) {
            sp.getPersistentData().putInt("rz_food_before", sp.getFoodData().getFoodLevel());
        }
    }

    public static void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        ItemStack used = event.getItem();
        if (used.is(RuletitaMod.TAZA_CAFE.get())) {
            event.setResultStack(new ItemStack(RuletitaMod.TAZA.get()));
            if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
                sp.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 1));
                sp.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 1200, 1));
                sp.addEffect(new MobEffectInstance(MobEffects.HASTE, 1200, 1));
                if (sp.level().getDayTime() % 24000L >= 12000L) {
                    sp.getPersistentData().putBoolean("rz_insomnio", true);
                    sp.displayClientMessage(Component.literal("Demasiado cafe a esta hora: esta noche no podras dormir"), true);
                }
            }
        } else if (entity instanceof ServerPlayer sp && used.get(DataComponents.FOOD) != null) {
            CompoundTag data = sp.getPersistentData();
            int hungerBefore = data.getIntOr("rz_food_before", -1);
            data.putInt("rz_food_before", -1);
            int hungerAfter = sp.getFoodData().getFoodLevel();
            // Los alimentos del mod son siempre comestibles. Se comprueba el nivel previo y,
            // como respaldo, el posterior por si otro mod omitió el evento Start.
            boolean consumedWhileFull = data.getBooleanOr("rz_lleno", false) || hungerBefore >= 20
                    || (hungerBefore < 0 && hungerAfter >= 20);
            data.putBoolean("rz_lleno", false);
            if (consumedWhileFull) {
                int previousFat = data.getIntOr("rz_grasa", 0);
                if (previousFat < MAX_GRASA) {
                    data.putInt("rz_grasa", previousFat + 1);
                }
            }
        }
    }

    public static void onSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.getPersistentData().getBooleanOr("rz_insomnio", false)) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("Tienes insomnio: no puedes dormir esta noche"), true);
            }
        }
    }
}
