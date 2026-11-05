package com.bossspawner;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafml.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("bossspawner")
public class BossSpawnerMod {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "bossspawner");

    public static final RegistryObject<Item> BOSS_SPAWNER = ITEMS.register("boss_spawner",
            () -> new BossSpawnerItem(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1)));

    public BossSpawnerMod() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
