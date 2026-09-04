package com.gtladd.gtladditions.common.data

import org.gtlcore.gtlcore.common.data.machines.AdvancedMultiBlockMachineA

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.RegistrateDisplayItemsGenerator

import net.minecraft.world.item.CreativeModeTab

import com.gtladd.gtladditions.GTLAdditions
import com.gtladd.gtladditions.api.registry.GTLAddRegistration.Companion.REGISTRATE
import com.gtladd.gtladditions.common.register.GTLAddItems
import com.tterrag.registrate.util.entry.RegistryEntry

object GTLAddCreativeModeTabs {
    @JvmField
    val GTLADD_ITEMS: RegistryEntry<CreativeModeTab> = REGISTRATE.defaultCreativeTab("item") {
        it.displayItems(RegistrateDisplayItemsGenerator("item", REGISTRATE))
            .title(REGISTRATE.addLang("itemGroup", GTLAdditions.id("item"), "GTLAdditions"))
            .icon { GTLAddItems.STARMETAL_BOULE.asStack() }
            .build()
    }.register()

    @JvmField
    val GTLADD_MACHINE: RegistryEntry<CreativeModeTab> = REGISTRATE.defaultCreativeTab("machine") {
        it.displayItems(RegistrateDisplayItemsGenerator("machine", REGISTRATE))
            .title(REGISTRATE.addLang("itemGroup", GTLAdditions.id("machine"), "GTLAdditions"))
            .icon { AdvancedMultiBlockMachineA.EYE_OF_HARMONY.asStack() }
            .build()
    }.register()

    @JvmStatic
    fun init() {}
}
