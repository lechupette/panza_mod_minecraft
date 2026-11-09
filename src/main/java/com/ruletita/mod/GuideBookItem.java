package com.ruletita.mod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Manual de consulta: al abrirlo muestra el índice interactivo de las láminas. */
public final class GuideBookItem extends Item {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            // Fuente del servidor con el jugador como entidad: no exige permisos de operador.
            level.getServer().getCommands().performPrefixedCommand(
                    level.getServer().createCommandSourceStack()
                            .withEntity(serverPlayer).withPosition(serverPlayer.position()),
                    "function panza:guia");
        }
        return InteractionResult.SUCCESS;
    }
}
