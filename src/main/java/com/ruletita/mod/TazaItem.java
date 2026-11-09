package com.ruletita.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Taza vacia: con clic derecho en agua se llena. Nunca coloca agua en el mundo. */
public class TazaItem extends Item {
    public TazaItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        ItemStack held = player.getItemInHand(hand);
        ItemStack filled = new ItemStack(RuletitaMod.TAZA_AGUA.get());
        if (held.getCount() <= 1) {
            player.setItemInHand(hand, filled);
        } else {
            held.shrink(1);
            if (!player.getInventory().add(filled)) player.drop(filled, false);
        }
        return InteractionResult.SUCCESS;
    }
}
