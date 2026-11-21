package com.example.carcass;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class CorpseBlockEntity extends BlockEntity {
    private final List<ItemStack> loot = new ArrayList<>();
    private String mobName = "";

    public CorpseBlockEntity(BlockPos pos, BlockState state) {
        super(Carcass.CORPSE_BE.get(), pos, state);
    }

    public void setLoot(List<ItemStack> stacks, String mobName) {
        loot.clear();
        for (ItemStack s : stacks) {
            if (!s.isEmpty()) loot.add(s.copy());
        }
        this.mobName = mobName;
        setChanged();
    }

    public List<ItemStack> getLoot() {
        return loot;
    }

    public String getMobName() {
        return mobName;
    }

    /** Забирає наступний шматок лута (один "розріз"). */
    public ItemStack takeNext() {
        if (loot.isEmpty()) return ItemStack.EMPTY;
        ItemStack s = loot.remove(0);
        setChanged();
        return s;
    }

    public boolean isEmpty() {
        return loot.isEmpty();
    }

    private static boolean isFood(ItemStack s) {
        return s.getItem().isEdible();
    }

    /** Псує їжу в трупі: на стадії 1 частину, на стадії 2 всю (перетворюється на гниле м'ясо). */
    public void rot(int stage, RandomSource random) {
        for (int i = 0; i < loot.size(); i++) {
            ItemStack s = loot.get(i);
            if (!isFood(s) || s.is(Items.ROTTEN_FLESH)) continue;
            if (stage >= 2 || random.nextFloat() < 0.4f) {
                loot.set(i, new ItemStack(Items.ROTTEN_FLESH, s.getCount()));
            }
        }
        setChanged();
    }

    /** Повне розкладання: уся їжа зникає, неїстівне (кістки, шкіра тощо) лишається. */
    public void removeFood() {
        loot.removeIf(CorpseBlockEntity::isFood);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ItemStack s : loot) {
            list.add(s.save(new CompoundTag()));
        }
        tag.put("Loot", list);
        tag.putString("Mob", mobName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loot.clear();
        ListTag list = tag.getList("Loot", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack s = ItemStack.of(list.getCompound(i));
            if (!s.isEmpty()) loot.add(s);
        }
        mobName = tag.getString("Mob");
    }
}
