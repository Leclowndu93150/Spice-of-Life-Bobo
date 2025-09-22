package com.leclowndu93150.spiceoflifebobo.enchantments;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, SpiceOfLifeBobo.MOD_ID);

    public static final RegistryObject<Enchantment> FOODIE = ENCHANTMENTS.register("foodie", FoodieEnchantment::new);
}