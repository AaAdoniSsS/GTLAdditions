---
navigation:
  title: Recursive Reverse Forge
  icon: recursive_reverse_forge
  parent: controller/multiblock_controller.md
  position: 10
item_ids:
  - recursive_reverse_forge
  - reverse_time_boosting_engine
  - catalytic_cascade_array
  - magnetorheological_convergence_core
  - hyperdimensional_energy_concentrator
  - fractal_manipulator
---

# Recursive Reverse Forge

<BlockImage id = "recursive_reverse_forge" scale = "8"/>

* Energy consumption factor: 0.8
* Maximum parallel number: 2,147,483,647
* Has [Multi Recipe Types](../multi_type.md): Dimensionally Transcendent Plasma Forge, Stellar Forge
* Prefect overclocking, and the number of overclocking is unlimited
* Only the laser chamber can be used
* Modules can be installed to obtain additional bonuses

### Reverse Time Boosting Engine

<BlockImage id = "reverse_time_boosting_engine" scale = "4"/>

* **It can only be installed on the structure of Recursive Reverse Forge, and only one of this module can be installed on each Recursive Reverse Forge**

> After installing the module, the recipe time can be reduced by a factor of: \
> <Latex math = "\min{(0.8, 0.05+0.8*e^{-3.2*input ratio^{1.9}})}" /> \
> The input ratio is:  input quantity multiply by 2/(initial recipe output quantity * current temperature), the input ratio will not be greater than 1, and the time reduction function will not be activated when the input ratio is less than 10% (The initial recipe output quantity refers to the output of a recipe run in 1 parallel with no additional bonuses)\
> Just input one output item or fluid. Do not use <FluidLink id="gtceu:dimensionallytranscendentresidue" />,<ItemLink id="kubejs:extremely_durable_plasma_cell" />,<ItemLink id="kubejs:time_dilation_containment_unit" />, <ItemLink id="kubejs:plasma_containment_cell" /> \
> The initial temperature is 48000K, if the machine is in the working state, the temperature will increase at the speed of 65K/t, and in the non-working state, the temperature will decrease at the speed of 900K/s (not less than 48000K) \
> Different fluids can be introduced to control the temperature. The heating medium can be <FluidLink id="minecraft:lava" />2500K,<FluidLink id="gtceu:blaze" />4600K,<FluidLink id="gtceu:raw_star_matter" />14000K, and the cooling fluid can be <FluidLink id="gtceu:ice" />1900K,<FluidLink id="gtceu:helium" />3400K,<FluidLink id="kubejs:gelid_cryotheum" />6700K \
> The consumption rates of both the heating medium and the coolant are 100B/1s \
> The input recipe products will be returned proportionally after the recipe processing is completed \
> When the temperature is within the range of [93000K, 97000K], the return ratio is 100% \
> When the temperature is lower than the lower limit of the range, the return ratio is:  \
> <Latex math = "0.5+0.5*\frac{current temperature-48000}{45000}" /> \
> When the temperature exceeds the upper limit of the range, the return ratio is: \
> <Latex math = "1-0.85*(\frac{current temperature-97000}{13000})^{0.42}" /> \
> The overheat protection mechanism will be triggered when the temperature exceeds 105000K. By default, the temperature is reduced at a cooling rate of 7125K/s (using coolant can increase the cooling rate additionally; the actual cooling rate is the default rate plus the coolant cooling rate). The Recursive Reverse Forge linked until it drops to 48000K will not handle the recipe \
> The <ItemLink id="gtladditions:vientiane_transcription_node" /> can be installed to control the temperature, similar to neutron sensor \
> **Only** use giant input bin to provide temperature control fluid. **Only** use <ItemLink id="super_input_dual_hatch" /> provide input to the recipe product

### Catalytic Cascade Array

<BlockImage id = "catalytic_cascade_array" scale = "4"/>

* **It can only be installed on the structure of Recursive Reverse Forge, and only one of this module can be installed on each Recursive Reverse Forge**

> After installing this module, the energy consumption of the recipe can be multiplied by 0.15, and the output of the recipe can be doubled \
> It has a 30-second catalyst **cycle period**. At the beginning of each catalyst cycle, the module will randomly output a redstone signal ranging from 1 to 15 \
> The output redstone signal needs to be received through <ItemLink id="gtladditions:vientiane_transcription_node" /> \
> **Only** use LV Huge Input Hatch to receive input catalyst \
> If the redstone signal is 1 to 3, it indicates that the catalyst used in this cycle is <FluidLink id="gtceu:dimensionallytranscendentcrudecatalyst" />. Following this pattern, the subsequent orders are <FluidLink id="gtceu:dimensionallytranscendentprosaiccatalyst" />, 
> <FluidLink id="gtceu:dimensionallytranscendentresplendentcatalyst" />, <FluidLink id="gtceu:dimensionallytranscendentexoticcatalyst" /> and <FluidLink id="gtceu:dimensionallytranscendentstellarcatalyst" /> \
> During the first 0 to 5 seconds of the cycle, the module will not function. After 5 seconds, the module will consume the catalyst based on the redstone signal output. The rate of catalyst consumption is 40B/s, and the effect will last until the fifth second of the next cycle \
> If the wrong catalyst is entered, there will be no increase in production for the rest of the cycle and any subsequent catalyst input will be consumed, and the energy reduction effect will be retained \
> The current and subsequent processing of formulas containing <ItemLink id="kubejs:extremely_durable_plasma_cell" />,<ItemLink id="kubejs:time_dilation_containment_unit" /> and <ItemLink id="kubejs:plasma_containment_cell" /> will not be included in the doubling process \
> **The Magnetorheological Convergence Core module has a higher priority than this module**

### Magnetorheological Convergence Core

<BlockImage id = "magnetorheological_convergence_core" scale = "4"/>

* **It can only be installed on the structure of Recursive Reverse Forge, and only one of this module can be installed on each Recursive Reverse Forge**

> After installing this module, the formula output can be focused on the first item and fluid, converting the output of the second product into additional output of the first product \
> It has a fuel input cycle of 12 seconds, and **only** two ULV hegu input buses and one LV huge input hatch can be used to receive the fuel \
> When the module is first activated, it will randomly select two items and one fluid from <ItemLink id="kubejs:black_body_naquadria_supersolid" />,<ItemLink id="kubejs:quantum_anomaly" />,<ItemLink id="kubejs:hyper_stable_self_healing_adhesive" />,<FluidLink id="gtceu:exciteddtec" /> and <FluidLink id="gtceu:exciteddtsc" /> as the fuel for maintaining the module's operation \
> The correct fuel selected by the module needs to be precisely input to activate the focusing function. If the input quantity is incorrect, an error message will be displayed in the module's GUI \
> At the same time, 2/s of Magmatter Block are fixedly consumed. If the input is insufficient, the formula cannot be focused and the output will be impossible \
> The current and subsequent processing of formulas containing <ItemLink id="kubejs:extremely_durable_plasma_cell" />,<ItemLink id="kubejs:time_dilation_containment_unit" /> and <ItemLink id="kubejs:plasma_containment_cell" /> will not be included in the doubling process \
> **The Reverse Time Boosting Engine module has a higher priority than this module**

### Hyperdimensional Energy Concentrator

<BlockImage id = "hyperdimensional_energy_concentrator" scale = "4"/>

* **It can only be installed on the structure of Recursive Reverse Forge, and only one of this module can be installed on each Recursive Reverse Forge**

> After the module is installed, the Recursive Reverse Forge can work directly by obtaining energy from the wireless energy network \
> The <ItemLink id="kubejs:hyperdimensional_drone" /> should be placed in the main module. Each time it is placed, the maximum energy obtainable will increase by 16A 
> <Color color="#FF0000">**M**</Color><Color color="#00FF00">**A**</Color><Color color="#0000FF">**X**</Color><Color color="#FFFF00">**+**</Color><Color color="#FF0000">**16**</Color> \
> The maximum power that can be obtained by the base is 1A MAX, and it needs to be put into the Hyperdimensional Drone to increase the upper limit of power(This will also lock the recipe level at MAX), and the maximum can be put into 64 \
> A hyperdimensional drone is launched (consumed) every hour to maintain connection with the wireless energy network \
> It is necessary to provide 524,288 CWU/t, 64A <Color color="#FF0000">**MAX**</Color> and 240B/s of Gelid Cryotheum to maintain the operation of the module

### Fractal Manipulator

<BlockImage id = "fractal_manipulator" scale = "4"/>

* **It can only be installed on the Recursive Reverse Forge structure, and each Recursive Reverse Forge can only accommodate a maximum of four of these modules**

> Energy consumption factor: 0.8 \
> Maximum parallel number: 2,147,483,647 \
> Prefect overclocking, and the number of overclocking is unlimited \
> Only the laser chamber can be used \
> It can share the Catalytic Cascade Array and the Hyperdimensional Energy Concentrator with the Recursive Reverse Forge \
> This module works **if and only if** the Reverse Time Boosting Engine module running perfectly \
> Each run requires the consumption of one accelerant, does not participate in parallel computing, and returns after the recipe is finished


