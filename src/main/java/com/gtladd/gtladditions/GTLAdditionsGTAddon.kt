package com.gtladd.gtladditions

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate

import net.minecraft.data.recipes.FinishedRecipe

import com.gtladd.gtladditions.api.registry.GTLAddRegistration
import com.gtladd.gtladditions.common.data.TooltipsModify
import com.gtladd.gtladditions.common.machine.GTLAddMachines
import com.gtladd.gtladditions.common.register.GTLAddItems
import com.gtladd.gtladditions.data.recipes.*
import com.gtladd.gtladditions.data.recipes.newmachinerecipe.*
import com.gtladd.gtladditions.data.recipes.process.SocProcess

import java.util.function.Consumer

@GTAddon
class GTLAdditionsGTAddon : IGTAddon {

    override fun getRegistrate(): GTRegistrate = GTLAddRegistration.REGISTRATE

    override fun addonModId(): String = GTLAdditions.MOD_ID

    override fun initializeAddon() {
        GTLAddItems.init()
        GTLAddMachines.init()
        TooltipsModify.init()
    }

    override fun addRecipes(provider: Consumer<FinishedRecipe>) {
        PhotonMatrixEtch.init(provider)
        EMResonanceConversionField.init(provider)
        TitansCripEarthbore.init(provider)
        BiologicalSimulation.init(provider)
        VoidfluxReaction.init(provider)
        StellarLgnition.init(provider)
        ChaosWeave.init(provider)
        FractalReconstruction.init(provider)
        AE2.init(provider)
        Assembler.init(provider)
        AssemblyLine.init(provider)
        Distort.init(provider)
        ElectricBlastFurnace.init(provider)
        IntegratedtedOreProcessor.init(provider)
        NewMachineRecipe1.init(provider)
        NewMachineRecipe2.init(provider)
        Qft.init(provider)
        SocProcess.init(provider)
        Misc.init(provider)
        RuriditExtend.init(provider)
        Ontokinetic.init(provider)
        EvolutionOfPrimordial.init(provider)
    }
}
