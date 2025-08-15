package com.leclowndu93150.spiceoflifebobo.data;

import net.minecraft.nbt.CompoundTag;

public class HealingPenalty {
    private int penaltyStacks;
    private int remainingDuration;

    public HealingPenalty(int penaltyStacks, int duration) {
        this.penaltyStacks = penaltyStacks;
        this.remainingDuration = duration;
    }

    public HealingPenalty() {
        this(0, 0);
    }

    public boolean tick() {
        if (remainingDuration <= 0) {
            return false;
        }

        remainingDuration--;
        
        if (remainingDuration <= 0) {
            penaltyStacks = 0;
            return false;
        }
        
        return true;
    }

    public void addStack(int duration, int maxStacks) {
        penaltyStacks = Math.min(penaltyStacks + 1, maxStacks);
        remainingDuration = duration; // Refresh duration
    }

    public int getPenaltyStacks() {
        return penaltyStacks;
    }

    public int getRemainingDuration() {
        return remainingDuration;
    }

    public double getHealingMultiplier(double penaltyPerStack) {
        if (penaltyStacks <= 0) {
            return 1.0;
        }
        
        // Each stack reduces healing by penaltyPerStack amount
        double reduction = penaltyStacks * penaltyPerStack;
        return Math.max(0.05, 1.0 - reduction); // Minimum 5% healing effectiveness
    }

    public boolean hasActive() {
        return penaltyStacks > 0 && remainingDuration > 0;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("penaltyStacks", penaltyStacks);
        tag.putInt("remainingDuration", remainingDuration);
        return tag;
    }

    public static HealingPenalty load(CompoundTag tag) {
        int penaltyStacks = tag.getInt("penaltyStacks");
        int remainingDuration = tag.getInt("remainingDuration");
        return new HealingPenalty(penaltyStacks, remainingDuration);
    }
}