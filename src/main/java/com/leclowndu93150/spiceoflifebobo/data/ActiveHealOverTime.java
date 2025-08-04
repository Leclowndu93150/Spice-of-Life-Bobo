package com.leclowndu93150.spiceoflifebobo.data;

import net.minecraft.nbt.CompoundTag;

public class ActiveHealOverTime {
    private int remainingDuration;
    private int interval;
    private double amountFlat;
    private double amountMax;
    private int ticksSinceLastHeal;

    public ActiveHealOverTime(int duration, int interval, double amountFlat, double amountMax) {
        this.remainingDuration = duration;
        this.interval = interval;
        this.amountFlat = amountFlat;
        this.amountMax = amountMax;
        this.ticksSinceLastHeal = 0;
    }

    public boolean tick() {
        if (remainingDuration <= 0) {
            return false;
        }

        ticksSinceLastHeal++;
        remainingDuration--;

        return remainingDuration > 0;
    }

    public boolean shouldHeal() {
        return ticksSinceLastHeal >= interval;
    }

    public void resetHealTimer() {
        ticksSinceLastHeal = 0;
    }

    public double getAmountFlat() {
        return amountFlat;
    }

    public double getAmountMax() {
        return amountMax;
    }

    public int getRemainingDuration() {
        return remainingDuration;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("remainingDuration", remainingDuration);
        tag.putInt("interval", interval);
        tag.putDouble("amountFlat", amountFlat);
        tag.putDouble("amountMax", amountMax);
        tag.putInt("ticksSinceLastHeal", ticksSinceLastHeal);
        return tag;
    }

    public static ActiveHealOverTime load(CompoundTag tag) {
        int remainingDuration = tag.getInt("remainingDuration");
        int interval = tag.getInt("interval");
        double amountFlat = tag.getDouble("amountFlat");
        double amountMax = tag.getDouble("amountMax");
        int ticksSinceLastHeal = tag.getInt("ticksSinceLastHeal");

        ActiveHealOverTime healOverTime = new ActiveHealOverTime(remainingDuration, interval, amountFlat, amountMax);
        healOverTime.ticksSinceLastHeal = ticksSinceLastHeal;
        return healOverTime;
    }
}