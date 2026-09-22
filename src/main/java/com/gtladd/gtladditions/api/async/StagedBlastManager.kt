package com.gtladd.gtladditions.api.async

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraftforge.event.TickEvent

import com.gtladd.gtladditions.GTLAdditions

class StagedBlastManager(private val level: ServerLevel) {
    private val active = ArrayList<StagedSphereExplosion>()
    private var persistCooldown = 0

    fun submit(blast: StagedSphereExplosion) {
        active.add(blast)
        persist()
    }

    fun tick(event: TickEvent.LevelTickEvent) {
        if (active.isEmpty()) return

        val iterator = active.iterator()
        var finishedAny = false
        while (iterator.hasNext()) {
            val blast = iterator.next()
            blast.tickFrame(event::haveTime)
            if (blast.isFinished) {
                iterator.remove()
                finishedAny = true
            }
        }

        if (finishedAny || --persistCooldown <= 0) {
            persistCooldown = PERSIST_INTERVAL_TICKS
            persist()
        }
    }

    fun onLevelUnload() {
        for (blast in active) blast.cancel()
        active.clear()
        persist()
    }

    private fun persist() {
        val data = level.dataStorage.computeIfAbsent(
            { tag -> BlastSavedData(tag) },
            { BlastSavedData() },
            DATA_NAME
        )
        val dimension = level.dimension().location().toString()
        val records = data.blasts.filterTo(ArrayList()) { it.dimension != dimension }
        for (blast in active) {
            records.add(
                BlastRecord(
                    dimension = dimension,
                    x = blast.centre.x,
                    y = blast.centre.y,
                    z = blast.centre.z,
                    radius = blast.radius,
                    progressOrderKey = blast.progressOrderKey,
                )
            )
        }
        data.blasts = records
        data.setDirty()
    }

    companion object {
        private const val DATA_NAME = GTLAdditions.MOD_ID + "_staged_blasts"
        private const val PERSIST_INTERVAL_TICKS = 20

        const val PROTECTED_RESISTANCE = 3_600_000f

        private val MANAGERS = HashMap<ResourceLocation, StagedBlastManager>()

        private fun manager(level: ServerLevel): StagedBlastManager = MANAGERS.getOrPut(level.dimension().location()) { StagedBlastManager(level) }

        @JvmStatic
        fun spawn(level: ServerLevel, centre: BlockPos, radius: Int, protectedResistance: Float? = PROTECTED_RESISTANCE) {
            manager(level).submit(
                StagedSphereExplosion(
                    level = level,
                    centre = centre,
                    radius = radius,
                    protectedResistance = protectedResistance,
                    removalsPerFrame = StagedSphereExplosion.removalsForRadius(radius),
                    sectionsPerFrame = StagedSphereExplosion.sectionsForRadius(radius),
                    chunkWindow = StagedSphereExplosion.chunkWindowForRadius(radius),
                    resumeOrderKey = null
                )
            )
        }

        @JvmStatic
        fun onLevelTick(event: TickEvent.LevelTickEvent) {
            if (!event.side.isServer) return
            val level = event.level as? ServerLevel ?: return
            MANAGERS[level.dimension().location()]?.tick(event)
        }

        @JvmStatic
        fun onLevelUnload(level: Level) {
            if (level !is ServerLevel) return
            MANAGERS.remove(level.dimension().location())?.onLevelUnload()
        }

        @JvmStatic
        fun restoreAll(levels: Iterable<ServerLevel>) {
            for (level in levels) restore(level)
        }

        private fun restore(level: ServerLevel) {
            val data = level.dataStorage.get({ tag -> BlastSavedData(tag) }, DATA_NAME) ?: return
            val dimension = level.dimension().location().toString()
            val records = data.blasts.filter { it.dimension == dimension && it.progressOrderKey >= 0 }
            if (records.isEmpty()) return
            val mgr = manager(level)
            for (record in records) {
                mgr.submit(
                    StagedSphereExplosion(
                        level = level,
                        centre = BlockPos(record.x, record.y, record.z),
                        radius = record.radius,
                        protectedResistance = PROTECTED_RESISTANCE,
                        removalsPerFrame = StagedSphereExplosion.removalsForRadius(record.radius),
                        sectionsPerFrame = StagedSphereExplosion.sectionsForRadius(record.radius),
                        chunkWindow = StagedSphereExplosion.chunkWindowForRadius(record.radius),
                        resumeOrderKey = record.progressOrderKey,
                    )
                )
            }
        }
    }

    class BlastSavedData(tag: CompoundTag) : SavedData() {
        var blasts: List<BlastRecord> = emptyList()

        init {
            val list = tag.getList("Blasts", NBT_TYPE_COMPOUND)
            val parsed = ArrayList<BlastRecord>(list.size)
            for (i in list.indices) parsed.add(BlastRecord.read(list.getCompound(i)))
            blasts = parsed
        }

        constructor() : this(CompoundTag())

        override fun save(tag: CompoundTag): CompoundTag {
            val list = ListTag()
            for (record in blasts) list.add(record.write())
            tag.put("Blasts", list)
            return tag
        }
    }

    class BlastRecord(
        val dimension: String,
        val x: Int,
        val y: Int,
        val z: Int,
        val radius: Int,
        val progressOrderKey: Long,
    ) {
        fun write(): CompoundTag = CompoundTag().apply {
            putString("Dimension", dimension)
            putInt("X", x)
            putInt("Y", y)
            putInt("Z", z)
            putInt("Radius", radius)
            putLong("Progress", progressOrderKey)
        }

        companion object {
            fun read(tag: CompoundTag) = BlastRecord(
                dimension = tag.getString("Dimension"),
                x = tag.getInt("X"),
                y = tag.getInt("Y"),
                z = tag.getInt("Z"),
                radius = tag.getInt("Radius"),
                progressOrderKey = tag.getLong("Progress"),
            )
        }
    }
}

private const val NBT_TYPE_COMPOUND = 10
