package com.gtladd.gtladditions.common.machine.multiblock.controller

import org.gtlcore.gtlcore.api.data.tag.GTLTagPrefix.nanoswarm
import org.gtlcore.gtlcore.api.machine.ISuspendableMachine
import org.gtlcore.gtlcore.api.recipe.RecipeResult
import org.gtlcore.gtlcore.api.recipe.RecipeResult.fail
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.handleRecipeOutput
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper.matchRecipeOutput
import org.gtlcore.gtlcore.common.data.GTLMaterials

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials

import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

import com.gtladd.gtladditions.api.machine.GTLAddWorkableElectricMultipleRecipesMachine
import com.gtladd.gtladditions.api.machine.logic.GTLAddMultipleRecipesLogic
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent

import java.util.function.BiPredicate

class BiologicalSimulationLaboratory(holder: IMachineBlockEntity) :
    GTLAddWorkableElectricMultipleRecipesMachine(holder), IMachineModifyDrops {

    @Persisted
    val machineStorage: NotifiableItemStackHandler = NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH) {
        object : ItemStackTransfer(1) {
            override fun getSlotLimit(slot: Int): Int = 1
        }
    }.setFilter(::filter).also { it.addChangedListener(this::upDataSlot) }

    @Persisted
    private var isMultiple: Boolean = false

    @Persisted
    private var reduceEUt = 1.0

    @Persisted
    private var reduceDuration = 1.0

    @Persisted
    private var maxParallels = 64

    @Persisted
    private var isMultiRecipes = false

    override fun createRecipeLogic(vararg args: Any) = BiologicalSimulationLaboratoryLogic(this)

    override fun getRecipeLogic() = super.getRecipeLogic() as BiologicalSimulationLaboratoryLogic

    override fun useModes() = false

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    fun filter(itemStack: ItemStack) = itemStack.`is`(NAN_CERTIFICATE) || itemStack.`is`(InfuscoliumNanoswarm) || itemStack.`is`(OrichalcumNanoswarm) || itemStack.`is`(RheniumNanoswarm)

    override fun createUIWidget(): Widget = (super.createUIWidget() as WidgetGroup).let {
        return it.addWidget(
            SlotWidget(
                machineStorage.storage,
                0,
                it.sizeWidth - 30,
                it.sizeHeight - 30,
                true,
                true
            ).setBackground(GuiTextures.SLOT)
        )
    }

    override fun modifyRecipe(recipe: GTRecipe): FastRecipeModify.ReduceResult {
        val multiplier = FastRecipeModify.ReduceResult(reduceEUt, reduceDuration)
        getRecipeLogic().setReduction(multiplier.reduceEUt, multiplier.reduceDuration)
        return multiplier
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        super.addDisplayText(textList)
        this.takeIf { it.isFormed }?.let {
            textList.add(Component.translatable("gtceu.machine.eut_multiplier.tooltip", reduceEUt))
            textList.add(Component.translatable("gtceu.machine.duration_multiplier.tooltip", reduceDuration))
        }.takeIf { this.isMultiRecipes }?.let {
            textList.add(
                Component.translatable("gtceu.machine.multiple_recipe.gui.0")
                    .append(
                        ComponentPanelWidget.withButton(
                            Component.literal("[")
                                .append(Component.translatable("gtceu.machine." + (if (this.isMultiple) "on" else "off")))
                                .append(Component.literal("]")),
                            "multiple_switch"
                        )
                    )
            )
        }
    }

    override fun handleDisplayClick(componentData: String, clickData: ClickData) {
        if (!clickData.isRemote && componentData == "multiple_switch") this.isMultiple = !this.isMultiple
    }

    override fun onDrops(drops: MutableList<ItemStack>) = clearInventory(this.machineStorage.storage)

    private fun upDataSlot() = when (this.machineStorage.storage.getStackInSlot(0).item) {
        RheniumNanoswarm -> setMachine(false, 2048, 0.9, 0.9)
        OrichalcumNanoswarm -> setMachine(false, 16384, 0.8, 0.6)
        InfuscoliumNanoswarm -> setMachine(false, 262144, 0.6, 0.4)
        NAN_CERTIFICATE -> setMachine(true, 4194304, 0.25, 0.1)
        else -> setMachine(false, 64, 1.0, 1.0)
    }

    private fun setMachine(isMultiRecipe: Boolean, maxParallels: Int, reduceEUt: Double, reduceDuration: Double) {
        this.isMultiRecipes = isMultiRecipe
        this.maxParallels = maxParallels
        this.reduceEUt = reduceEUt
        this.reduceDuration = reduceDuration
        getRecipeLogic().setReduction(reduceEUt, reduceDuration)
    }

    override fun getMaxParallel(): Int = maxParallels

    class BiologicalSimulationLaboratoryLogic(val bslMachine: BiologicalSimulationLaboratory) :
        GTLAddMultipleRecipesLogic(bslMachine) {

        override fun findAndHandleRecipe() {
            lastRecipe = null
            (if (this.isMultipleRecipe) getMultipleRecipe else this.oneRecipe)?.let {
                if (matchRecipeOutput(this.bslMachine, it)) setupRecipe(it)
            }
        }

        val isMultipleRecipe: Boolean get() = bslMachine.machineStorage.storage.getStackInSlot(0).`is`(NAN_CERTIFICATE) && bslMachine.isMultiple

        val oneRecipe: GTRecipe?
            get() {
                if (!bslMachine.hasProxies()) return null
                (bslMachine.recipeType.lookup.find(bslMachine, this::checkRecipe))?.let {
                    return FastRecipeModify.modify(
                        bslMachine,
                        it,
                        parallel.maxParallel.toLong(),
                        ocResult = FastRecipeModify.getPerfectOverclock()
                    ) { recipe -> bslMachine.modifyRecipe(recipe) }
                }
                return null
            }

        override fun onRecipeFinish() {
            lastRecipe?.let { handleRecipeOutput(this.bslMachine, it) }
            if (machine is ISuspendableMachine) {
                val ism = machine as ISuspendableMachine
                if (ism.`gtlcore$isSuspendAfterFinish`()) {
                    this.status = Status.SUSPEND
                    ism.`gtlcore$setSuspendAfterFinish`(false)
                } else {
                    (if (this.isMultipleRecipe) getMultipleRecipe else this.oneRecipe)?.let {
                        if (matchRecipeOutput(this.bslMachine, it)) setupRecipe(it)
                        return
                    }
                    status = Status.IDLE
                }
            }
            progress = 0
            duration = 0
        }

        override fun checkRecipe(recipe: GTRecipe): Boolean = BeforeTest.test(recipe, bslMachine) && super.checkRecipe(recipe)
    }

    companion object {
        private val RheniumNanoswarm = ChemicalHelper.get(nanoswarm, GTMaterials.Rhenium).item
        private val OrichalcumNanoswarm = ChemicalHelper.get(nanoswarm, GTLMaterials.Orichalcum).item
        private val InfuscoliumNanoswarm = ChemicalHelper.get(nanoswarm, GTLMaterials.Infuscolium).item
        private val NAN_CERTIFICATE: Item by lazy { GTItems.NAN_CERTIFICATE.asItem() }
        private val BeforeTest = BiPredicate { recipe: GTRecipe, machine: IRecipeLogicMachine ->
            (machine as BiologicalSimulationLaboratory).let {
                if (recipe.data.contains("infinity") && !it.isMultiRecipes) {
                    RecipeResult.of(it, fail("gtceu.biological_simulation.infinity.0".toComponent))
                    return@BiPredicate false
                }
                return@BiPredicate true
            }
            return@BiPredicate false
        }
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(BiologicalSimulationLaboratory::class.java, GTLAddWorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER)
    }
}
