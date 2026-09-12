package com.gtladd.gtladditions.api.recipe

object GTLAddBatchDisabledDefinitions {

    @JvmField
    val DISABLED_DEFINITION_IDS: Set<String> = setOf(
        "gtladditions:atomic_transmutation_core",
        "gtladditions:time_space_distorter",
        "gtladditions:skeleton_shift_rift_engine",
        "gtladditions:draconic_collapse_core",
        "gtladditions:taixu_turbid_array",
        "gtladditions:recursive_reverse_forge",
        "gtladditions:fractal_manipulator",
        "gtladditions:titan_crip_earthbore",
        "gtladditions:space_elevator_mkii"
    )

    @JvmStatic
    fun isDisabled(definitionId: String): Boolean = DISABLED_DEFINITION_IDS.contains(definitionId)
}
