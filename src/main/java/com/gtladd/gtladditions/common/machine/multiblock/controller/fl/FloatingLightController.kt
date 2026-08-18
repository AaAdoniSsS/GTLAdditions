package com.gtladd.gtladditions.common.machine.multiblock.controller.fl

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineHost
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule
import org.gtlcore.gtlcore.common.data.GTLMaterials
import org.gtlcore.gtlcore.utils.datastructure.ModuleRenderInfo

import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine

import com.lowdragmc.lowdraglib.gui.widget.*
import com.lowdragmc.lowdraglib.side.fluid.FluidStack

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.material.Fluid

import com.gtladd.gtladditions.api.machine.IEnergyMachine
import com.gtladd.gtladditions.api.machine.gui.MultiblockDisplayText
import com.gtladd.gtladditions.common.machine.multiblock.MultiBlockMachine
import com.gtladd.gtladditions.common.machine.multiblock.controller.fl.FloatingLightPosHelper.calculateModulePositions
import com.gtladd.gtladditions.utils.ComponentUtil.toComponent
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

import java.util.function.Consumer

class FloatingLightController(holder: IMachineBlockEntity) :
    WorkableElectricMultiblockMachine(holder),
    IModularMachineHost<FloatingLightController>,
    IMachineLife {

    val modulePos = ObjectOpenHashSet<IModularMachineModule<FloatingLightController, FloatingLightModule>>(32)
    private var fluidHatch: FluidHatchPartMachine? = null
    var isDouble = false

    fun getCircuit() = IntCircuitBehaviour.getCircuitConfiguration(fluidHatch?.circuitInventory?.getStackInSlot(0) ?: ItemStack.EMPTY)

    override fun createRecipeLogic(vararg args: Any) = FLRecipeLogic(this)

    override fun onStructureFormed() {
        super.onStructureFormed()
        parts.forEach { if (it is FluidHatchPartMachine) this.fluidHatch = it }
        safeClearModules()
        scanAndConnectModules()
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        safeClearModules()
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

    override fun attachSideTabs(sideTabs: TabsWidget) {
        sideTabs.setMainTab(this)
        CombinedDirectionalFancyConfigurator.of(self(), self())?.let { sideTabs.attachSubTab(it) }
    }

    override fun createUIWidget(): Widget {
        val group = WidgetGroup(0, 0, 182 + 8, 117 + 8)
        group.addWidget(
            DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(screenTexture)
                .addWidget(LabelWidget(4, 5, self().blockState.block.descriptionId))
                .addWidget(
                    ComponentPanelWidget(4, 17, ::addDisplayText)
                        .textSupplier(if (this.level!!.isClientSide) null else Consumer(::addDisplayText))
                        .setMaxWidthLimit(150)
                )
        )
        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    override fun addDisplayText(textList: MutableList<Component>) {
        MultiblockDisplayText.builder(textList, isFormed)
            .setWorkingStatus(recipeLogic.isWorkingEnabled, true)
            .addEnergyTierLine(tier)
            .addWorkingStatusLine()
            .addComponent(Component.translatable("gtceu.machine.module", modulePos.size))
    }

    override fun onMachineRemoved() = safeClearModules()

    override fun getModuleSet() = this.modulePos

    override fun getModuleScanPositions(): Array<out BlockPos> = calculateModulePositions(pos, frontFacing)

    override fun isFormed() = this.isFormed

    override fun getMaxModuleCount() = 32

    override fun getModulesForRendering() = listOf(
        ModuleRenderInfo(
            BlockPos(-14, 28, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 28, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 28, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 28, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        ),
        ModuleRenderInfo(
            BlockPos(-14, 34, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 34, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 34, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 34, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        ),
        ModuleRenderInfo(
            BlockPos(-14, 40, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 40, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 40, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 40, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        ),
        ModuleRenderInfo(
            BlockPos(-14, 46, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 46, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 46, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 46, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        ),
        ModuleRenderInfo(
            BlockPos(-14, 52, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 52, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 52, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 52, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        ),
        ModuleRenderInfo(
            BlockPos(-14, 58, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 58, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 58, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 58, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        ),
        ModuleRenderInfo(
            BlockPos(-14, 64, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 64, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 64, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 64, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        ),
        ModuleRenderInfo(
            BlockPos(-14, 70, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_1
        ),
        ModuleRenderInfo(
            BlockPos(-14, 70, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.WEST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_2
        ),
        ModuleRenderInfo(
            BlockPos(14, 70, 8),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.UP,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_3
        ),
        ModuleRenderInfo(
            BlockPos(14, 70, 12),
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.DOWN,
            MultiBlockMachine.FLOATING_LIGHT_DEEP_SPACE_INDUSTRIAL_VESSEL_MODULE_4
        )
    )

    class FLRecipeLogic(val flMachine: FloatingLightController) : RecipeLogic(flMachine) {
        override fun serverTick() {
            if (!this.isSuspend) {
                if (this.progress < 20) this.handleRecipeWorking()
                if (this.progress >= 20) progress = 0
            } else if (this.subscription != null) {
                this.subscription.unsubscribe()
                this.subscription = null
            }
        }

        override fun handleRecipeWorking() {
            val ecList = (flMachine as IEnergyMachine).energyContainerList
            if (flMachine.maxVoltage > 0 && flMachine.maxVoltage <= ecList.energyStored) {
                ecList.removeEnergy(flMachine.maxVoltage)
                this.status = Status.WORKING
                ++this.progress
                if (progress == 3) {
                    if (flMachine.getCircuit() == 2) {
                        val fluid = flMachine.fluidHatch?.tank?.getFluidInTank(0) ?: FluidStack.empty()
                        if (!fluid.isEmpty && fluid.fluid == RAWSTARMATTER && fluid.amount >= 1000) {
                            flMachine.fluidHatch?.tank?.drainInternal(1000, false)
                            flMachine.isDouble = true
                        } else {
                            flMachine.isDouble = false
                        }
                    } else {
                        flMachine.isDouble = false
                    }
                }
            } else {
                this.status = Status.SUSPEND
            }
        }

        override fun updateSound() = Unit
    }

    companion object {
        val RAWSTARMATTER: Fluid = GTLMaterials.RawStarMatter.getFluid(FluidStorageKeys.PLASMA)
    }
}
