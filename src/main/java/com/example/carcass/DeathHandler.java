package com.example.carcass;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class DeathHandler {

    @SubscribeEvent
    public void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity.level() instanceof ServerLevel level)) return;
        if (!(entity instanceof Mob)) return;                               // гравців не чіпаємо
        if (entity instanceof WitherBoss || entity instanceof EnderDragon) return; // боси як у ванілі

        List<ItemStack> loot = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack s = drop.getItem();
            if (!s.isEmpty()) loot.add(s.copy());
        }
        if (loot.isEmpty()) return;

        BlockPos spot = Carcass.findSpot(level, entity.blockPosition());
        if (spot == null) return; // нема місця під труп: лут випаде як у ванілі

        level.setBlockAndUpdate(spot, Carcass.CORPSE.get().defaultBlockState());
        if (level.getBlockEntity(spot) instanceof CorpseBlockEntity be) {
            be.setLoot(loot, entity.getType().getDescriptionId());
            event.setCanceled(true); // ванільний лут не випадає
        }
    }
}
