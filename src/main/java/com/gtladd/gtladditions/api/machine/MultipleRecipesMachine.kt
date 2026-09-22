package com.gtladd.gtladditions.api.machine

import org.gtlcore.gtlcore.api.machine.trait.IBatchMachine
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.common.machine.multiblock.electric.WorkableElectricMultipleRecipesMachine

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.recipe.GTRecipe

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.network.chat.Component

import com.gtladd.gtladditions.api.machine.gui.GTLAddMultiRecipeMachineConfigurator
import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.api.machine.logic.MultipleRecipesLogic
import com.gtladd.gtladditions.api.recipe.FastRecipeModify
import com.gtladd.gtladditions.api.recipe.ledger.RecipeSearchContext
import com.gtladd.gtladditions.config.ConfigHolder

open class MultipleRecipesMachine(holder: IMachineBlockEntity) :
    WorkableElectricMultipleRecipesMachine(holder), IGTLAddMachine, IBatchMachine, IRecipeSearchProvider {

    private var searchCtx: RecipeSearchContext? = null

    override fun getSearchContext(): RecipeSearchContext? = searchCtx

    override fun setSearchContext(ctx: RecipeSearchContext?) {
        searchCtx = ctx
    }

    @Persisted
    private var limitDuration = ConfigHolder.INSTANCE.limitDuration

    @Persisted
    private var machineMode = ConfigHolder.INSTANCE.isMultiple.isMultiple

    companion object {
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(
            MultipleRecipesMachine::class.java,
            WorkableElectricMultipleRecipesMachine.MANAGED_FIELD_HOLDER
        )
    }

    open fun getThread(): Int = 128

    open fun testBefore(obj: Any) = true

    open fun modifyRecipe(recipe: GTRecipe): FastRecipeModify.ReduceResult = FastRecipeModify.getDefaultReduce()

    open fun getOverClock(): FastRecipeModify.OverClockFactor = FastRecipeModify.getPerfectOverclock()

    override fun isBatchEnabled(): Boolean = false

    override fun setBatchEnabled(p0: Boolean) {}

    override fun supportsBatchProcessing(): Boolean = false

    override fun canConfigureBatchProcessing(): Boolean = false

    public override fun createRecipeLogic(vararg args: Any) = MultipleRecipesLogic(this)

    override fun getRecipeLogic() = super.getRecipeLogic() as MultipleRecipesLogic

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        searchCtx = null
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(recipeLogic.isWorkingEnabled, recipeLogic.isActive)
            .addEnergyUsageLine(energyContainer)
            .addEnergyTierLine(tier)
            .addMachineModeLine(recipeType)
            .addParallelsLine(maxParallel)
            .addWorkingStatusLine()
            .addProgressLine(recipeLogic.progressPercent)
            .addRecipeStatus(recipeLogic as IRecipeStatus)
        this.definition.additionalDisplay.accept(this, textList)
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        super.attachConfigurators(configuratorPanel)
        configuratorPanel.attachConfigurators(GTLAddMultiRecipeMachineConfigurator(this))
    }

    override var limitedDuration: Int
        get() = this.limitDuration
        set(value) {
            this.limitDuration = value
        }

    override var isMultipleMode: Boolean
        get() = this.machineMode
        set(mode) {
            this.machineMode = mode
        }
}
