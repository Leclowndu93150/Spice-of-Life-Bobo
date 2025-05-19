package com.leclowndu93150.spiceoflifebobo;

import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import com.leclowndu93150.spiceoflifebobo.capability.FoodStorageCapability;
import com.leclowndu93150.spiceoflifebobo.events.ClientEvents;
import com.leclowndu93150.spiceoflifebobo.events.FoodEvents;
import com.leclowndu93150.spiceoflifebobo.events.PlayerEvents;
import com.leclowndu93150.spiceoflifebobo.items.ModItems;
import com.leclowndu93150.spiceoflifebobo.manager.FoodEffectManager;
import com.leclowndu93150.spiceoflifebobo.networking.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(SpiceOfLifeBobo.MOD_ID)
public class SpiceOfLifeBobo {

    public static final String MOD_ID = "spiceoflifebobo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MOD_ID);

    public static final RegistryObject<Attribute> FOOD_MEMORY = ATTRIBUTES.register("food_memory",
            () -> new RangedAttribute("attribute.spiceoflifebobo.food_memory", 3.0D, 1.0D, 10.0D));
    //.setDescription("How many different foods you can benefit from at once"));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpiceOfLifeBobo.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SPICE_OF_LIFE_TAB = CREATIVE_MODE_TABS.register("spice_of_life_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LAXATIVE.get()))
                    .title(Component.translatable("itemGroup.spiceoflifebobo"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.LAXATIVE.get());
                    })
                    .build()
    );

    public static final Capability<IFoodStorage> FOOD_STORAGE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private static FoodEffectManager foodEffectManager;


    public SpiceOfLifeBobo() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        // DON'T register capabilities here - it's already being done in FoodStorageCapability.RegistrationHandler

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpiceOfLifeConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpiceOfLifeConfig.CLIENT_SPEC);

        ModItems.ITEMS.register(modEventBus);

        ATTRIBUTES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new FoodEvents());
        MinecraftForge.EVENT_BUS.register(new PlayerEvents());
        // Register capability events
        MinecraftForge.EVENT_BUS.register(FoodStorageCapability.EventHandler.class);

        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::init);
        foodEffectManager = new FoodEffectManager();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(foodEffectManager);
    }

    public static FoodEffectManager getFoodEffectManager() {
        return foodEffectManager;
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}