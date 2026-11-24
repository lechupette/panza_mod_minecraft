package com.ruletita.mod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

/**
 * Comandos de administración y pruebas de Panza (solo operadores).
 *
 * <pre>
 * /panza estado [jugador]
 * /panza grasa    set|add|clear [valor] [jugador]
 * /panza musculo  set|add|clear [valor] [jugador]
 * /panza hambre   set|add [valor] [jugador]
 * /panza reiniciar [jugador]
 * </pre>
 * Sin jugador, actúan sobre quien ejecuta el comando.
 */
public final class Comandos {
    private Comandos() { }

    /** Qué hacer con el valor indicado. */
    private enum Modo { SET, ADD, CLEAR }

    @FunctionalInterface
    private interface Ejecutor {
        int ejecutar(CommandContext<CommandSourceStack> contexto, ServerPlayer objetivo) throws CommandSyntaxException;
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("panza")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(conObjetivo(Commands.literal("estado"), Comandos::estado))
                .then(variable("grasa", Eventos.MAX_GRASA, true,
                        p -> p.getPersistentData().getIntOr("rz_grasa", 0),
                        (p, valor) -> {
                            p.getPersistentData().putInt("rz_grasa", valor);
                            if (valor == 0) p.getPersistentData().putInt("rz_gacc", 0);
                        }))
                .then(variable("musculo", Eventos.MAX_MUSCULO, true,
                        p -> p.getPersistentData().getIntOr("rz_musc", 0),
                        (p, valor) -> {
                            CompoundTag d = p.getPersistentData();
                            d.putInt("rz_musc", valor);
                            d.putInt("rz_mpts", 0);
                            d.putInt("rz_pura", 0);
                            d.putInt("rz_warn", 0);
                        }))
                .then(variable("hambre", 20, false,
                        p -> p.getFoodData().getFoodLevel(),
                        (p, valor) -> p.getFoodData().setFoodLevel(valor)))
                .then(conObjetivo(Commands.literal("reiniciar"), Comandos::reiniciar)));
    }

    /** Agrega a un nodo la ejecución sobre uno mismo y, opcionalmente, sobre otro jugador. */
    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T conObjetivo(T nodo, Ejecutor ejecutor) {
        return nodo
                .executes(c -> ejecutor.ejecutar(c, c.getSource().getPlayerOrException()))
                .then(Commands.argument("jugador", EntityArgument.player())
                        .executes(c -> ejecutor.ejecutar(c, EntityArgument.getPlayer(c, "jugador"))));
    }

    /** Subárbol "nombre set|add|clear" para una variable numérica del jugador. */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> variable(
            String nombre, int max, boolean conClear, ToIntFunction<ServerPlayer> leer, BiConsumer<ServerPlayer, Integer> escribir) {
        var raiz = Commands.literal(nombre);
        raiz.then(Commands.literal("set").then(conObjetivo(
                Commands.argument("valor", IntegerArgumentType.integer(0, max)),
                (c, p) -> cambiar(c, p, nombre, max, leer, escribir, Modo.SET, IntegerArgumentType.getInteger(c, "valor")))));
        raiz.then(Commands.literal("add").then(conObjetivo(
                Commands.argument("cantidad", IntegerArgumentType.integer(1, max)),
                (c, p) -> cambiar(c, p, nombre, max, leer, escribir, Modo.ADD, IntegerArgumentType.getInteger(c, "cantidad")))));
        if (conClear) {
            raiz.then(conObjetivo(Commands.literal("clear"),
                    (c, p) -> cambiar(c, p, nombre, max, leer, escribir, Modo.CLEAR, 0)));
        }
        return raiz;
    }

    private static int cambiar(CommandContext<CommandSourceStack> c, ServerPlayer p, String nombre, int max,
                               ToIntFunction<ServerPlayer> leer, BiConsumer<ServerPlayer, Integer> escribir, Modo modo, int n) {
        int antes = leer.applyAsInt(p);
        int nuevo = switch (modo) {
            case SET -> n;
            case ADD -> antes + n;
            case CLEAR -> 0;
        };
        nuevo = Math.max(0, Math.min(max, nuevo));
        escribir.accept(p, nuevo);
        final int resultado = nuevo;
        c.getSource().sendSuccess(() -> Component.literal(
                p.getName().getString() + " · " + nombre + ": " + antes + " → " + resultado + " / " + max), true);
        return 1;
    }

    private static int estado(CommandContext<CommandSourceStack> c, ServerPlayer p) {
        CompoundTag d = p.getPersistentData();
        int grasa = d.getIntOr("rz_grasa", 0);
        int musculo = d.getIntOr("rz_musc", 0);
        int progreso = d.getIntOr("rz_mpts", 0) * 100 / Eventos.PUNTOS_NIVEL;
        String zona = grasa == 0 ? "sin grasa" : (grasa <= Eventos.GRASA_UTIL ? "zona útil" : "panza pura");
        StringBuilder texto = new StringBuilder("Estado de " + p.getName().getString());
        texto.append("\nGrasa: ").append(grasa).append(" / ").append(Eventos.MAX_GRASA).append(" (").append(zona).append(")");
        texto.append("\nMúsculo: ").append(musculo).append(" / ").append(Eventos.MAX_MUSCULO);
        if (musculo < Eventos.MAX_MUSCULO) texto.append(" (siguiente nivel: ").append(progreso).append(" %)");
        texto.append("\nHambre: ").append(p.getFoodData().getFoodLevel()).append(" / 20");
        if (d.getIntOr("rz_warn", 0) == 1) texto.append("\nAviso: la panza se está comiendo tus músculos");
        c.getSource().sendSuccess(() -> Component.literal(texto.toString()), false);
        return 1;
    }

    private static int reiniciar(CommandContext<CommandSourceStack> c, ServerPlayer p) {
        CompoundTag d = p.getPersistentData();
        for (String clave : new String[] {"rz_grasa", "rz_gacc", "rz_dec", "rz_musc", "rz_mpts", "rz_pura", "rz_warn", "rz_fat_n", "rz_fat_src"}) {
            d.putInt(clave, 0);
        }
        c.getSource().sendSuccess(() -> Component.literal("Grasa y músculo de " + p.getName().getString() + " reiniciados."), true);
        return 1;
    }
}
