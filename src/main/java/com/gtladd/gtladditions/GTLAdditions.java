package com.gtladd.gtladditions;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.gtladd.gtladditions.api.registry.GTLAddRegistration;
import com.gtladd.gtladditions.common.data.GTLAddCreativeModeTabs;
import com.gtladd.gtladditions.common.machine.GTLAddMachines;
import com.gtladd.gtladditions.common.recipe.GTLAddRecipesTypes;
import com.gtladd.gtladditions.common.register.GTLAddMaterial;
import com.gtladd.gtladditions.config.ConfigHolder;

@Mod(GTLAdditions.MOD_ID)
public class GTLAdditions {

    public static final String MOD_ID = "gtladditions";
    public static final String NAME = "GTLAdditions";

    public static ResourceLocation id(String name) {
        return new ResourceLocation(GTLAdditions.MOD_ID, name);
    }

    public GTLAdditions() {
        init();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addMaterialRegistries);
        modEventBus.addListener(this::addMaterials);
        GTLAddRegistration.REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
    }

    public static void init() {
        GTLAddCreativeModeTabs.init();
        ConfigHolder.init();
    }

    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(GTLAdditions.MOD_ID);
    }

    private void addMaterials(MaterialEvent event) {
        GTLAddMaterial.init();
    }

    @SubscribeEvent
    public void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTLAddRecipesTypes.init();
    }

    @SubscribeEvent
    public void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GTLAddMachines.init();
    }
}
