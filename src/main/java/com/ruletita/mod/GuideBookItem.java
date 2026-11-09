package com.ruletita.mod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
        if (!level.isClientSide() && level.getServer() != null) {
            level.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(), "function ruletita:guia");
        }
        return InteractionResult.SUCCESS;
    }
}
