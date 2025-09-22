package com.leclowndu93150.spiceoflifebobo;

import com.leclowndu93150.spiceoflifebobo.api.IFoodStorage;
import com.leclowndu93150.spiceoflifebobo.capability.FoodStorageCapability;
import com.leclowndu93150.spiceoflifebobo.enchantments.ModEnchantments;
import com.leclowndu93150.spiceoflifebobo.events.ClientEvents;
import com.leclowndu93150.spiceoflifebobo.events.EnchantmentEvents;
import com.leclowndu93150.spiceoflifebobo.events.FoodEvents;
import com.leclowndu93150.spiceoflifebobo.events.HealingEvents;
import com.leclowndu93150.spiceoflifebobo.events.PlayerEvents;
import com.leclowndu93150.spiceoflifebobo.items.ModItems;
import com.leclowndu93150.spiceoflifebobo.manager.FoodEffectManager;
import com.leclowndu93150.spiceoflifebobo.manager.HealingItemManager;
import com.leclowndu93150.spiceoflifebobo.networking.NetworkHandler;
import com.leclowndu93150.spiceoflifebobo.networking.SyncDatapacksPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
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

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpiceOfLifeBobo.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SPICE_OF_LIFE_TAB = CREATIVE_MODE_TABS.register("spice_of_life_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LAXATIVE.get()))
                    .title(Component.translatable("itemGroup.spiceoflifebobo"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.LAXATIVE.get());
                        
                        ItemStack enchantedBook1 = new ItemStack(Items.ENCHANTED_BOOK);
                        enchantedBook1.enchant(ModEnchantments.FOODIE.get(), 1);
                        output.accept(enchantedBook1);
                        
                        ItemStack enchantedBook2 = new ItemStack(Items.ENCHANTED_BOOK);
                        enchantedBook2.enchant(ModEnchantments.FOODIE.get(), 2);
                        output.accept(enchantedBook2);
                        
                        ItemStack enchantedBook3 = new ItemStack(Items.ENCHANTED_BOOK);
                        enchantedBook3.enchant(ModEnchantments.FOODIE.get(), 3);
                        output.accept(enchantedBook3);
                    })
                    .build()
    );

    public static final Capability<IFoodStorage> FOOD_STORAGE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private static FoodEffectManager foodEffectManager;
    private static HealingItemManager healingItemManager;


    public SpiceOfLifeBobo() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::setupAttributes);


        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SpiceOfLifeConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SpiceOfLifeConfig.CLIENT_SPEC);

        ModItems.ITEMS.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);

        ATTRIBUTES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new FoodEvents());
        MinecraftForge.EVENT_BUS.register(new HealingEvents());
        MinecraftForge.EVENT_BUS.register(new PlayerEvents());
        MinecraftForge.EVENT_BUS.register(new EnchantmentEvents());
        MinecraftForge.EVENT_BUS.register(FoodStorageCapability.EventHandler.class);

        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::onDatapackSync);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::init);
        foodEffectManager = new FoodEffectManager();
        healingItemManager = new HealingItemManager();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void setupAttributes(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, FOOD_MEMORY.get());
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(foodEffectManager);
        event.addListener(healingItemManager);
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        SyncDatapacksPacket datapackPacket = new SyncDatapacksPacket(
                foodEffectManager.getAllEffects(),
                foodEffectManager.getAllFoodEffects(),
                healingItemManager.getAllHealingItems(),
                healingItemManager.getAllItemToHealingItem()
        );

        if (event.getPlayer() != null) {
            NetworkHandler.sendToPlayer(datapackPacket, event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(player -> {
                NetworkHandler.sendToPlayer(datapackPacket, player);
            });
        }
    }

    public static FoodEffectManager getFoodEffectManager() {
        return foodEffectManager;
    }

    public static HealingItemManager getHealingItemManager() {
        return healingItemManager;
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}