package com.ruletita.mod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

/** Logica de servidor: flatulencias, grasa/panza, cafe e insomnio. */
public final class Eventos {
    private static final int MAX_GRASA = 6;
    private static final Identifier MOD_VELOCIDAD = Identifier.fromNamespaceAndPath(RuletitaMod.MODID, "grasa");

    private Eventos() { }

    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (!(event.player() instanceof ServerPlayer sp)) return;
        CompoundTag d = sp.getPersistentData();
        int food = sp.getFoodData().getFoodLevel();
        int grasa = d.getIntOr("rz_grasa", 0);

        // --- flatulencias (30 s - 2 min, el doble de frecuentes con 8+ muslos)
        int cd = d.getIntOr("rz_cd", 0);
        if (cd <= 0) cd = 600 + sp.getRandom().nextInt(1801);
        cd -= (food >= 16 ? 2 : 1);
        if (cd <= 0) {
            flatulencia(sp);
            cd = 600 + sp.getRandom().nextInt(1801);
        }
        d.putInt("rz_cd", cd);

        // --- la grasa baja 1 punto cada 5 minutos
        int dec = d.getIntOr("rz_dec", 0) + 1;
        if (dec >= 6000) {
            dec = 0;
            if (grasa > 0) { grasa--; d.putInt("rz_grasa", grasa); }
        }
        d.putInt("rz_dec", dec);

        // --- el insomnio termina al amanecer
        if (sp.level().getDayTime() % 24000L < 12000L) d.putBoolean("rz_insomnio", false);

        // --- nivel de panza (0-13) -> equipo "rzbN" que los clientes leen para dibujarla
        int nivel = Math.max(0, Math.min(7, food - 13)) + grasa;
        if (nivel != d.getIntOr("rz_nivel", -1)) {
            d.putInt("rz_nivel", nivel);
            asignarEquipo(sp, nivel);
        }

        // --- lentitud segun grasa
        if (grasa != d.getIntOr("rz_vel", -1)) {
            d.putInt("rz_vel", grasa);
            AttributeInstance inst = sp.getAttribute(Attributes.MOVEMENT_SPEED);
            if (inst != null) {
                inst.removeModifier(MOD_VELOCIDAD);
                if (grasa > 0) {
                    inst.addPermanentModifier(new AttributeModifier(MOD_VELOCIDAD, -0.03 * grasa, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
        }

        // --- barra de grasa (3 muslos = 6 puntos) en la action bar
        if (grasa > 0 && sp.tickCount % 20 == 0) {
            StringBuilder sb = new StringBuilder("Grasa ");
            for (int i = 0; i < MAX_GRASA; i++) sb.append(i < grasa ? "\u25A0" : "\u25A1");
            sp.displayClientMessage(Component.literal(sb.toString()), true);
        }
    }

    private static void flatulencia(ServerPlayer sp) {
        ServerLevel level = (ServerLevel) sp.level();
        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), RuletitaMod.FART.get(), SoundSource.PLAYERS,
                1.0f, 0.9f + sp.getRandom().nextFloat() * 0.3f);
        level.sendParticles(ParticleTypes.POOF, sp.getX(), sp.getY() + 0.6, sp.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
        level.sendParticles(ParticleTypes.SNEEZE, sp.getX(), sp.getY() + 0.6, sp.getZ(), 4, 0.25, 0.1, 0.25, 0.01);
    }

    private static void asignarEquipo(ServerPlayer sp, int nivel) {
        MinecraftServer server = sp.level().getServer();
        if (server == null) return;
        Scoreboard sb = server.getScoreboard();
        String nombre = "rzb" + nivel;
        PlayerTeam team = sb.getPlayerTeam(nombre);
        if (team == null) team = sb.addPlayerTeam(nombre);
        sb.addPlayerToTeam(sp.getScoreboardName(), team);
    }

    public static void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        ItemStack usado = event.getItem();
        if (usado.is(RuletitaMod.TAZA_CAFE.get())) {
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
        } else if (entity instanceof ServerPlayer sp && usado.get(DataComponents.FOOD) != null) {
            // comer hasta quedar lleno suma grasa
            if (sp.getFoodData().getFoodLevel() >= 20) {
                CompoundTag d = sp.getPersistentData();
                d.putInt("rz_grasa", Math.min(MAX_GRASA, d.getIntOr("rz_grasa", 0) + 1));
            }
        }
    }

    public static void onSleep(PlayerSleepInBedEvent event) {
        Player p = event.getEntity();
        if (p.level().isClientSide()) return;
        if (p.getPersistentData().getBooleanOr("rz_insomnio", false)) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            if (p instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("Tienes insomnio: no puedes dormir esta noche"), true);
            }
        }
    }
}
