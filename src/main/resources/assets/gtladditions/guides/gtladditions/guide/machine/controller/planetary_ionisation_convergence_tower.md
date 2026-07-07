---
navigation:
  title: Planetary Ionisation Convergence Tower
  icon: planetary_ionisation_convergence_tower
  parent: controller/multiblock_controller.md
  position: 10
item_ids:
  - planetary_ionisation_convergence_tower
---

# Planetary Ionisation Convergence Tower

<BlockImage id = "planetary_ionisation_convergence_tower" scale = "8"/>


* Only coils of Titan-grade or higher may be used;
* The operational cycle lasts 3 seconds;
* If energy continues to enter whilst the internal energy buffer is full, a massive explosion will occur centred on the machine;
* At the start of the operational cycle, an instantaneous, extremely high-power EU pulse (1 tick) is injected into the internal energy buffer, followed by a smooth discharge of lower power into the buffer for the remainder of the cycle;
* After the pulse ends, the internal energy buffer will output power to the external environment via the power bay/laser source bay:
* The Stellar Thermodynamic Container tier affects the internal energy buffer capacity:
> Basic: 54,120,000,000,000 EU \
> Advanced: 3,475,000,000,000,000 EU\
> Ultimate: 1,160,000,000,000,000,000 EU
* Coil tier affects the type of fluid consumed, the consumption rate per cycle, and power generation (power generation data is based on the highest-tier coil in the group; for each tier reduction, both instantaneous and discharge values are divided by 16)
> Titan Steel to Exquisite Gold: <FluidLink id="gtceu:rhenium" /> 73,728 mB, <FluidLink id="gtceu:ice" /> 8 KB, <ItemLink id="kubejs:space_drone_mk2" /> 2×10⁻⁴ units \
> Instantaneous - 4096A MAX, Discharge - 16A MAX\
> Super-Silicon Rock – Talon to Starlight: <FluidLink id="gtceu:promethium" /> 36,864 MB, <FluidLink id="gtceu:liquid_helium" /> 4 KB, <ItemLink id="kubejs:space_drone_mk4" /> 1×10⁻⁴ units \
> Instantaneous - 524,288A MAX Discharge - 256A MAX\
> Endless to Eternity: <FluidLink id="gtceu:crystalmatrix" /> 9216 MB, <FluidLink id="kubejs:gelid_cryotheum" /> 1 KB, <ItemLink id="kubejs:space_drone_mk6" /> 2.5×10⁻⁵ units \
> Instantaneous -268435456A MAX Discharge -131072A MAX
* Right-click to insert one into the machine via the handheld <ItemLink id="gtmthings:creative_laser_hatch"/> to activate special overclocking mode:
> The operating cycle is reduced to 1s; all energy is output directly to the wireless grid, and the maximum single-shot output current is increased to 64A MAX+16, regardless of coil level and internal buffer
* Material consumption and types at this time are:
> <FluidLink id="gtceu:miracle" /> 10mB, <ItemLink id="kubejs:hyperdimensional_drone" /> 1x10^(-6) units
* **Note**: If the host is removed, the special overclocking mode will be reset and the Creative Laser Hatch will not be returned.