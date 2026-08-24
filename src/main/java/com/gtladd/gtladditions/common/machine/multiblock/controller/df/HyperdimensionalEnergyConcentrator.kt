package com.gtladd.gtladditions.common.machine.multiblock.controller.df

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus
import org.gtlcore.gtlcore.common.machine.multiblock.electric.StorageMachine

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.gregtechceu.gtceu.utils.FormattingUtil

import com.lowdragmc.lowdraglib.side.fluid.FluidStack
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult

import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.Cryotheum
import com.gtladd.gtladditions.common.machine.multiblock.controller.Resource.HyperdimensionalDrone
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import com.gtladd.gtladditions.utils.MachineUtil.inputFluidStack
import com.hepdd.gtmthings.api.misc.WirelessEnergyManager
import com.hepdd.gtmthings.utils.TeamUtil

import java.math.BigInteger
import java.util.*

class HyperdimensionalEnergyConcentrator(holder: IMachineBlockEntity) :
    StorageMachine(holder, 64),
    IModularMachineModule<RecursiveReverseForge, HyperdimensionalEnergyConcentrator>,
    IMachineLife {

    private val startupSubs = ConditionalSubscriptionHandler(this, this::startupUpdate) { this.isFormed }

    @Persisted
    var uuid: UUID? = null

    @Persisted
    private var tick = 0

    @Persisted
    private var hostPosition: BlockPos? = null
    private var host: RecursiveReverseForge? = null

    private fun startupUpdate() {
        if (offsetTimer % 20L == 0L) {
            tick++
            this.inputFluidStack(Fluid)
            if (tick >= 3600) {
                tick = 0
                machineStorage.extractItemInternal(0, 1, false)
            }
            if (tick % 5 == 0) findAndConnectToHost()
        }
        updateHostTierBasedOnDrones()
    }

    fun getEUt(): BigInteger {
        if (!isWorkingEnabled) return BigInteger.ZERO
        val count = machineStorage.getStackInSlot(0).count
        return WirelessEnergyManager.getUserEU(uuid)
            .min(if (count == 0) bigInteger else bigLong.multiply(BigInteger.valueOf(count * 16L)))
    }

    fun consumeWirelessEU(eut: BigInteger) = WirelessEnergyManager.addEUToGlobalEnergyMap(uuid, eut, this)

    override fun filter(itemStack: ItemStack) = itemStack.`is`(HyperdimensionalDrone)

    override fun attachSideTabs(sideTabs: TabsWidget) {
        sideTabs.setMainTab(this)
        CombinedDirectionalFancyConfigurator.of(self(), self())?.let { sideTabs.attachSubTab(it) }
    }

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        configuratorPanel.attachConfigurators(
            IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.0, 1.0, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0.0, 0.5, 1.0, 0.5),
                { this.isWorkingEnabled },
                { _, pressed -> this.isWorkingEnabled = pressed }
            )
                .setTooltipsSupplier { listOf(if (it) "behaviour.soft_hammer.enabled".toComponent else "behaviour.soft_hammer.disabled".toComponent) }
        )
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        val builder = MultiblockDisplayText.builder(textList, isFormed())
            .setWorkingStatus(recipeLogic.isWorkingEnabled, recipeLogic.isActive)
            .addWorkingStatusLine()
            .addProgressLine(recipeLogic.progressPercent)
            .addRecipeStatus(recipeLogic as IRecipeStatus)
        builder.addComponent(Component.translatable("gtceu.machine.recursive_reverse_forge.gui.module.4", if (host == null) "×" else "✓"))
        if (uuid != null) {
            builder.addComponent(
                Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", TeamUtil.GetName(level, uuid)),
                Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.1", FormattingUtil.formatNumbers(WirelessEnergyManager.getUserEU(uuid))),
                Component.translatable("gtceu.machine.hyperdimensional_energy_concentrator.gui.tooltip.1", FormattingUtil.formatNumbers(getEUt())).withStyle { style ->
                    style.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "gtceu.multiblock.max_energy_per_tick_hover".toComponent.withStyle(ChatFormatting.GRAY)))
                }
            )
        }
    }

    override fun onStructureFormed() {
        super.onStructureFormed()
        startupSubs.initialize(level)
        if (!findAndConnectToHost()) removeFromHost(this.host)
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        removeFromHost(this.host)
    }

    override fun onPartUnload() {
        super.onPartUnload()
        removeFromHost(this.host)
    }

    override fun onMachineRemoved() = removeFromHost(this.host)

    override fun onMachinePlaced(player: LivingEntity?, stack: ItemStack) {
        player?.let { this.uuid = it.uuid }
    }

    override fun shouldOpenUI(player: Player, hand: InteractionHand, hit: BlockHitResult): Boolean {
        if (this.uuid == null || this.uuid != player.uuid) this.uuid = player.uuid
        return true
    }

    override fun createRecipeLogic(vararg args: Any) = HECRecipeLogic(this)

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    override fun getHostPosition() = this.hostPosition

    override fun setHostPosition(pos1: BlockPos?) {
        this.hostPosition = pos1
    }

    override fun getHost() = this.host

    override fun setHost(reverseForge: RecursiveReverseForge?) {
        this.host = reverseForge
    }

    override fun getHostType() = RecursiveReverseForge::class.java

    override fun getHostScanPositions(): Array<BlockPos> {
        val x = when (frontFacing) {
            Direction.EAST -> -33
            Direction.WEST -> 33
            else -> 0
        }
        val z = when (frontFacing) {
            Direction.NORTH -> 33
            Direction.SOUTH -> -33
            else -> 0
        }
        return arrayOf(pos.offset(x, 7, z))
    }

    override fun onConnected(host: RecursiveReverseForge) = recipeLogic.updateTickSubscription()

    companion object {
        val recipe: GTRecipe by lazy { GTRecipeBuilder.ofRaw().CWUt(524288).buildRawRecipe() }
        const val EU = 64L * Int.MAX_VALUE
        val bigLong: BigInteger = BigInteger.valueOf(Long.MAX_VALUE)
        val bigInteger: BigInteger = BigInteger.valueOf(Integer.MAX_VALUE.toLong())
        private val Fluid = FluidStack.create(Cryotheum, 240000)
        val MANAGED_FIELD_HOLDER = ManagedFieldHolder(HyperdimensionalEnergyConcentrator::class.java, StorageMachine.MANAGED_FIELD_HOLDER)
    }

    class HECRecipeLogic(val hecMachine: HyperdimensionalEnergyConcentrator) : RecipeLogic(hecMachine) {

        override fun serverTick() {
            if (!this.isSuspend) {
                if (this.progress < 1200) this.handleRecipeWorking()
                if (this.progress >= 1200) progress = 0
            } else if (this.subscription != null) {
                this.subscription.unsubscribe()
                this.subscription = null
            }
        }

        override fun handleRecipeWorking() {
            val ecList = (hecMachine as IEnergyMachine).energyContainerList
            if (EU <= ecList.energyStored && recipe.matchRecipeContents(IO.IN, machine, recipe.tickInputs, true).isSuccess) {
                recipe.handleTickRecipeIO(IO.IN, machine, Collections.emptyMap())
                ecList.changeEnergy(-EU)
                this.status = Status.WORKING
                this.lastRecipe = recipe
                ++this.progress
            } else {
                this.setWaiting(null)
            }
            if (this.status == Status.WAITING) this.doDamping()
        }
    }

    fun hasDrone(): Boolean {
        return machineStorage.getStackInSlot(0).count > 0
    }

    @Persisted
    private var lastHasDrone = false

    private fun updateHostTierBasedOnDrones() {
        val hasDrone = hasDrone()
        if (hasDrone != lastHasDrone) {
            lastHasDrone = hasDrone
            if (host != null) {
                host!!.getRecipeLogic().updateTickSubscription()
            }
        }
    }
}
