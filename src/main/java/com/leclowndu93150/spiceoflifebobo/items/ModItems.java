package com.leclowndu93150.spiceoflifebobo.items;

import com.leclowndu93150.spiceoflifebobo.SpiceOfLifeBobo;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SpiceOfLifeBobo.MOD_ID);

    public static final RegistryObject<Item> LAXATIVE = ITEMS.register("laxative",
            () -> new LaxativeItem(new Item.Properties()));
}
