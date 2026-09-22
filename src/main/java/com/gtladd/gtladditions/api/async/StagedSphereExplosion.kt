package com.gtladd.gtladditions.api.async

import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.TicketType
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunkSection
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.AABB
import net.minecraft.world.ticks.LevelTicks

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet

import kotlin.math.sqrt

class StagedSphereExplosion(
    val level: ServerLevel,
    val centre: BlockPos,
    val radius: Int,

    private val protectedResistance: Float?,

    private val removalsPerFrame: Int,

    private val sectionsPerFrame: Int,

    private val chunkWindow: Int,

    resumeOrderKey: Long?,
) {
    private class SectionTask(
        val sx: Int,
        val sy: Int,
        val sz: Int,
        val distSq: Long,

        val orderKey: Long,
    )

    private val ready = ArrayDeque<SectionTask>()

    private var nextShellLow = 0L

    private var shellExhausted = false

    private val pendingTickets = HashMap<Long, ChunkPos>()

    private var frontier = -1L
    private var removed: Long = 0
    private var finished = false

    private var resumeAt = 0

    private var soundCooldown = 0
    private var openingEmitted = false
    private var stallFrames = 0

    private val probe = BlockPos.MutableBlockPos()

    private val below = BlockPos.MutableBlockPos()

    private val cursor = BlockPos.MutableBlockPos()

    private val sectionChanges = ShortOpenHashSet()

    private var guardBox: BoundingBox? = null

    private var guardTicksLeft = 0

    private var sweepPhase = false

    private var sweepDone = false

    private var sweepPasses = 0

    private var passVaporised = 0L

    private var verifyRequested = false

    private var verifyPhase = false

    private var verifyPass = 0

    private var passLeftovers = 0L

    private var lastScanCount = 0

    private var guardTailExtended = 0

    private var fallingBox: AABB? = null

    private var mainLoopSections = 0

    var progressOrderKey: Long = resumeOrderKey ?: -1L
        private set

    val isFinished: Boolean get() = finished && guardTicksLeft <= 0 && sweepDone

    private fun hasPendingWork(): Boolean = !shellExhausted || ready.isNotEmpty()

    init {
        require(radius > 0) { "blast radius must be positive" }

        if (resumeOrderKey != null) {
            decodeOrderKey(resumeOrderKey).let { (nsx, nsy, nsz) ->
                val dx = nearestAxisDistance(nsx shl 4, (nsx shl 4) + 15, centre.x)
                val dy = nearestAxisDistance(nsy shl 4, (nsy shl 4) + 15, centre.y)
                val dz = nearestAxisDistance(nsz shl 4, (nsz shl 4) + 15, centre.z)
                frontier = dx * dx + dy * dy + dz * dz

                nextShellLow = (frontier / SHELL_BAND) * SHELL_BAND
                progressOrderKey = resumeOrderKey
            }
        }
    }

    private fun generateShell(fromDist: Long, innerLowSq: Long = 0L): List<SectionTask> {
        val rLong = radius.toLong()
        val r2 = rLong * rLong
        val out = ArrayList<SectionTask>(1024)
        val minSectionY = level.minSection
        val maxSectionY = level.minSection + level.sectionsCount - 1
        val cx = centre.x
        val cy = centre.y
        val cz = centre.z

        val minNsx = floorDiv((cx - radius).toLong(), 16).toInt()
        val maxNsx = floorDiv((cx + radius).toLong(), 16).toInt()
        val minNsz = floorDiv((cz - radius).toLong(), 16).toInt()
        val maxNsz = floorDiv((cz + radius).toLong(), 16).toInt()

        for (nsx in minNsx..maxNsx) {
            val sx = nsx shl 4
            val dxNear = nearestAxisDistance(sx, sx + 15, cx)
            val rxz = dxNear * dxNear
            if (rxz > r2) continue

            for (nsz in minNsz..maxNsz) {
                val sz = nsz shl 4
                val dzNear = nearestAxisDistance(sz, sz + 15, cz)
                val rxz2 = rxz + dzNear * dzNear
                if (rxz2 > r2) continue

                val dyAllow = isqrt(r2 - rxz2)
                val nsyLoWorld = floorDiv(cy - dyAllow, 16).toInt()
                val nsyHiWorld = floorDiv(cy + dyAllow, 16).toInt()
                val nsyLo = maxOf(minSectionY, nsyLoWorld - 1)
                val nsyHi = minOf(maxSectionY, nsyHiWorld + 1)
                if (nsyLo > nsyHi) continue

                var nsy = nsyLo
                while (nsy <= nsyHi) {
                    val yBase = (nsy shl 4).toLong()

                    val yLow = maxOf(yBase, cy - dyAllow)
                    val yHigh = minOf(yBase + 15L, cy + dyAllow)
                    if (yLow <= yHigh) {
                        val ox = (sx + 8 - cx).toLong()
                        val oy = yBase + 8L - cy
                        val oz = (sz + 8 - cz).toLong()
                        val c2 = ox * ox + oy * oy + oz * oz

                        if (c2 < innerLowSq) {
                            nsy++
                            continue
                        }
                        if (c2 in fromDist until (fromDist + SHELL_BAND)) {
                            out.add(SectionTask(nsx, nsy, nsz, c2, orderKey(nsx, nsy, nsz)))
                        }
                    }
                    nsy++
                }
            }
        }

        return out.sortedBy { mortonCode(it.sx, it.sy, it.sz) }
    }

    fun tickFrame(hasTime: () -> Boolean) {
        applyInflowGuard()
        if (finished) {
            if (guardTicksLeft > 0) {
                guardTicksLeft--
                if (guardTicksLeft > 0) holdGuardForFalling()
                if (guardTicksLeft == 0) {
                    if (verifyRequested) startVerify() else startSweep()
                }
                return
            }
        }
        var sectionBudget = sectionsPerFrame

        var removalBudget =
            if (sweepPhase || verifyPhase) removalsPerFrame * SWEEP_SCAN_BUDGET_FACTOR else removalsPerFrame
        var advanced = false
        var blocked = false

        while (sectionBudget > 0 && removalBudget > 0) {
            if (advanced && !hasTime()) break

            if (ready.isEmpty()) {
                if (shellExhausted) break

                val band = generateShell(nextShellLow, if (sweepPhase) sweepLowSq() else 0L)
                nextShellLow += SHELL_BAND
                if (nextShellLow > shellLimitSq(radius) + SHELL_BAND) shellExhausted = true
                if (band.isEmpty()) continue
                for (i in band.indices.reversed()) ready.addFirst(band[i])
            }

            val task = ready.first()
            val chunk = level.chunkSource.getChunkNow(task.sx, task.sz)
            if (chunk == null) {
                if (verifyPhase) {
                    ready.removeFirst()
                    resumeAt = 0
                    sectionBudget--
                    advanced = true
                    continue
                }

                reserveWindow(task.sx, task.sz)
                blocked = true
                break
            }

            val index = level.getSectionIndexFromSectionY(task.sy)
            val chunkSections: Array<LevelChunkSection> = chunk.sections
            var truncatedAt = 0
            if (index >= 0 && index < chunkSections.size) {
                val removedBefore = removed
                if (sweepPhase || verifyPhase) {
                    truncatedAt = sweepSection(chunkSections[index], task, removalBudget, resumeAt, verifyPhase)
                } else {
                    mainLoopSections++

                    truncatedAt = applySection(task, removalBudget, resumeAt)
                }

                flushSectionChanges(chunkSections[index], task)

                removalBudget -=
                    if (sweepPhase || verifyPhase) lastScanCount else (removed - removedBefore).toInt()
            }

            if (truncatedAt > 0) {
                resumeAt = truncatedAt
                sectionBudget--
                advanced = true
            } else {
                ready.removeFirst()
                resumeAt = 0
                sectionBudget--
                advanced = true

                if (!sweepPhase && !verifyPhase) {
                    progressOrderKey = task.orderKey
                    frontier = maxOf(frontier, task.distSq)
                    if (mainLoopSections % 32 == 0) pruneWindow(task.distSq)
                }
            }
        }

        if (advanced) {
            stallFrames = 0
            ready.firstOrNull()?.let {
                reserveWindow(it.sx, it.sz)
                pruneWindow(it.distSq)
            }

            if (!sweepPhase && !verifyPhase) emitEffects()
        } else if (blocked || hasPendingWork()) {
            if (++stallFrames >= MAX_STALL_FRAMES) {
                if (verifyPhase) {
                    endVerifyPass()
                } else if (sweepPhase) {
                    finishSweepGiveUp()
                } else {
                    releaseAllTickets()
                    guardTicksLeft = INFLOW_GUARD_COOLDOWN_TICKS
                    finished = true
                }
            }
        }

        if (!hasPendingWork()) {
            when {
                verifyPhase -> endVerifyPass()
                sweepPhase -> endSweepPass()
                else -> finish()
            }
        }
    }

    private fun applySection(task: SectionTask, budget: Int, startAt: Int): Int {
        val r = radius
        val r2 = r.toLong() * r
        val baseX = task.sx shl 4
        val baseY = task.sy shl 4
        val baseZ = task.sz shl 4

        val yLow = centre.y - r
        val yHigh = centre.y + r
        if (baseY + 15 < yLow || baseY > yHigh) return 0
        val ly0 = maxOf(0, yLow - baseY)
        val ly1 = minOf(15, yHigh - baseY)
        val cx = centre.x
        val cy = centre.y
        val cz = centre.z

        val nearSurfaceSq = (r - 1).toLong() * (r - 1)

        val farX = farthestAxisDistance(baseX, baseX + 15, cx)
        val farY = farthestAxisDistance(baseY, baseY + 15, cy)
        val farZ = farthestAxisDistance(baseZ, baseZ + 15, cz)

        val insideReach = (r - 2).toLong().coerceAtLeast(0L)
        val fullyInside = farX * farX + farY * farY + farZ * farZ <= insideReach * insideReach &&
            ly0 == 0 && ly1 == 15

        if (fullyInside) {
            return applySectionInterior(task, budget, startAt)
        }

        val pos = BlockPos.MutableBlockPos()

        var i = startAt
        var here = 0
        while (i < 4096) {
            val lx = i shr 8
            val ly = (i shr 4) and 15
            val lz = i and 15
            i++

            if (ly !in ly0..ly1) continue

            val dx = baseX + lx - cx
            val dx2 = dx.toLong() * dx
            if (dx2 > r2) continue

            val dy = baseY + ly - cy
            val dx2dy2 = dx2 + dy.toLong() * dy
            if (dx2dy2 > r2) continue

            val dz = baseZ + lz - cz
            val d2 = dx2dy2 + dz.toLong() * dz
            if (d2 > r2) continue

            pos.set(baseX + lx, baseY + ly, baseZ + lz)
            if (clearBlockAt(pos)) {
                here++

                if (d2 > nearSurfaceSq) settleUnsupportedNeighbours(pos, dx, dy, dz, d2, r2)
            }
            if (here >= budget) return i
        }
        return 0
    }

    private fun applySectionInterior(task: SectionTask, budget: Int, startAt: Int): Int {
        val baseX = task.sx shl 4
        val baseY = task.sy shl 4
        val baseZ = task.sz shl 4
        val pos = BlockPos.MutableBlockPos()

        var i = startAt
        var here = 0
        while (i < 4096) {
            val lx = i shr 8
            val ly = (i shr 4) and 15
            val lz = i and 15
            i++

            pos.set(baseX + lx, baseY + ly, baseZ + lz)

            if (clearBlockAt(pos)) here++
            if (here >= budget) return i
        }
        return 0
    }

    private fun clearBlockAt(pos: BlockPos.MutableBlockPos): Boolean {
        val state = level.getBlockState(pos)
        if (state.isAir) return false

        @Suppress("DEPRECATION")
        val resistance = state.block.getExplosionResistance()
        if (protectedResistance != null && resistance >= protectedResistance) return false

        level.setBlock(pos, AIR_STATE, FLAG_QUIET_NO_SYNC)
        sectionChanges.add(SectionPos.sectionRelativePos(pos))
        removed++
        return true
    }

    private fun applyInflowGuard() {
        val box = guardBox ?: guardBoxOf().also { guardBox = it }
        val fluidTicks: LevelTicks<Fluid> = level.fluidTicks
        fluidTicks.clearArea(box)
    }

    private fun guardBoxOf(): BoundingBox {
        val r = radius
        val margin = INFLOW_GUARD_MARGIN
        return BoundingBox(
            centre.x - r - margin,
            level.minBuildHeight,
            centre.z - r - margin,
            centre.x + r + margin,
            level.maxBuildHeight - 1,
            centre.z + r + margin
        )
    }

    private fun flushSectionChanges(section: LevelChunkSection, task: SectionTask) {
        if (sectionChanges.isEmpty) return
        val players = level.chunkSource.chunkMap.getPlayers(ChunkPos(task.sx, task.sz), false)
        if (players.isEmpty()) {
            sectionChanges.clear()
            return
        }
        val packet = ClientboundSectionBlocksUpdatePacket(
            SectionPos.of(task.sx, task.sy, task.sz),
            sectionChanges,
            section
        )
        sectionChanges.clear()
        for (player in players) {
            player.connection.send(packet)
        }
    }

    private fun settleUnsupportedNeighbours(pos: BlockPos.MutableBlockPos, dx: Int, dy: Int, dz: Int, d2: Long, r2: Long) {
        var k = 0
        while (k < 6) {
            val ox = NEIGHBOUR_OFFSETS[k * 3]
            val oy = NEIGHBOUR_OFFSETS[k * 3 + 1]
            val oz = NEIGHBOUR_OFFSETS[k * 3 + 2]
            k++

            if (d2 + 2L * (dx * ox + dy * oy + dz * oz) + 1L <= r2) continue
            probe.set(pos.x + ox, pos.y + oy, pos.z + oz)
            val state = level.getBlockState(probe)
            if (state.isAir) continue
            if (state.block is LiquidBlock) continue
            if (!state.canSurvive(level, probe)) {
                level.setBlock(probe, AIR_STATE, FLAG_QUIET_NO_SYNC)
                level.sendBlockUpdated(probe, state, AIR_STATE, FLAG_CLIENT_ONLY)
                continue
            }

            if (state.block is FallingBlock) {
                below.set(probe.x, probe.y - 1, probe.z)
                if (FallingBlock.isFree(level.getBlockState(below))) collapseFallingColumn(probe)
            }
        }
    }

    private fun collapseFallingColumn(start: BlockPos.MutableBlockPos) {
        var y = start.y
        val maxY = level.maxBuildHeight
        while (y < maxY) {
            cursor.set(start.x, y, start.z)
            val state = level.getBlockState(cursor)
            if (state.block !is FallingBlock) break
            below.set(start.x, y - 1, start.z)

            if (!FallingBlock.isFree(level.getBlockState(below))) break
            level.setBlock(cursor, AIR_STATE, FLAG_QUIET_NO_SYNC)
            level.sendBlockUpdated(cursor, state, AIR_STATE, FLAG_CLIENT_ONLY)
            y++
        }

        if (y < maxY) {
            cursor.set(start.x, y, start.z)
            val top = level.getBlockState(cursor)
            if (!top.isAir && top.block !is LiquidBlock && !top.canSurvive(level, cursor)) {
                level.setBlock(cursor, AIR_STATE, FLAG_QUIET_NO_SYNC)
                level.sendBlockUpdated(cursor, top, AIR_STATE, FLAG_CLIENT_ONLY)
            }
        }
    }

    private fun reserveWindow(cx: Int, cz: Int) {
        val cr = (chunkWindow shr 4) + 1
        val chunkSource = level.chunkSource
        for (dx in -cr..cr) {
            for (dz in -cr..cr) {
                val pos = ChunkPos(cx + dx, cz + dz)
                val key = pos.toLong()
                if (pendingTickets.containsKey(key)) continue
                chunkSource.addRegionTicket(TicketType.FORCED, pos, 2, pos)
                pendingTickets[key] = pos
            }
        }
    }

    private fun pruneWindow(frontDistSq: Long) {
        if (pendingTickets.isEmpty()) return
        val chunkSource = level.chunkSource
        val keep = chunkWindow.toLong() * chunkWindow
        val iterator = pendingTickets.values.iterator()
        while (iterator.hasNext()) {
            val pos = iterator.next()
            if (distanceSqToChunk(pos) > frontDistSq + keep) {
                chunkSource.removeRegionTicket(TicketType.FORCED, pos, 2, pos)
                iterator.remove()
            }
        }
    }

    private fun distanceSqToChunk(pos: ChunkPos): Long {
        val dx = nearestAxisDistance(pos.minBlockX, pos.minBlockX + 15, centre.x)
        val dz = nearestAxisDistance(pos.minBlockZ, pos.minBlockZ + 15, centre.z)
        return dx * dx + dz * dz
    }

    private fun emitEffects() {
        if (!openingEmitted) {
            openingEmitted = true
            level.playSound(
                null,
                centre.x.toDouble(),
                centre.y.toDouble(),
                centre.z.toDouble(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                48.0f,
                0.5f
            )
        }

        if (--soundCooldown > 0) return
        soundCooldown = EFFECT_INTERVAL_TICKS

        val front = ready.firstOrNull() ?: return
        level.playSound(
            null,
            ((front.sx shl 4) + 8).toDouble(),
            ((front.sy shl 4) + 8).toDouble(),
            ((front.sz shl 4) + 8).toDouble(),
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.BLOCKS,
            8.0f,
            0.6f + (front.distSq % 7) * 0.05f
        )
    }

    private fun holdGuardForFalling() {
        if (guardTicksLeft % FALLING_POLL_INTERVAL != 0 && guardTicksLeft > 1) return
        if (guardTailExtended >= MAX_GUARD_TAIL_EXTEND) return
        val falling = countFallingNearSphere()
        if (falling == 0) return
        guardTicksLeft += FALLING_POLL_INTERVAL
        guardTailExtended += FALLING_POLL_INTERVAL
    }

    private fun fallingCheckBox(): AABB {
        val h = radius.toDouble() + INFLOW_GUARD_MARGIN
        val v = radius.toDouble() + FALLING_CHECK_Y_MARGIN
        return AABB(
            centre.x - h,
            centre.y - v,
            centre.z - h,
            centre.x + h,
            centre.y + v,
            centre.z + h
        )
    }

    private fun sweepDepth(): Int {
        val shift = (sweepPasses - 1).coerceIn(0, 24)
        val raw = SWEEP_DEPTH.toLong() shl shift
        return raw.coerceAtMost(radius.toLong()).toInt().coerceAtLeast(1)
    }

    private fun sweepLowSq(): Long {
        val low = radius.toLong() - sweepDepth() - SECTION_REACH
        return if (low <= 0L) 0L else low * low
    }

    private fun sweepInnerSq(): Long {
        val low = radius.toLong() - sweepDepth()
        return if (low <= 0L) 0L else low * low
    }

    private fun startSweep() {
        sweepPasses++
        passVaporised = 0L
        sweepPhase = true
        ready.clear()
        nextShellLow = sweepLowSq()
        shellExhausted = false
        resumeAt = 0
        stallFrames = 0
    }

    private fun sweepSection(section: LevelChunkSection, task: SectionTask, budget: Int, startAt: Int, anyBlock: Boolean = false): Int {
        lastScanCount = 0

        if (section.hasOnlyAir()) return 0

        val r = radius
        val r2 = r.toLong() * r

        val innerSq = if (anyBlock) 0L else sweepInnerSq()
        val baseX = task.sx shl 4
        val baseY = task.sy shl 4
        val baseZ = task.sz shl 4

        val yLow = centre.y - r
        val yHigh = centre.y + r
        if (baseY + 15 < yLow || baseY > yHigh) return 0
        val ly0 = maxOf(0, yLow - baseY)
        val ly1 = minOf(15, yHigh - baseY)

        val cx = centre.x
        val cy = centre.y
        val cz = centre.z
        val pos = BlockPos.MutableBlockPos()

        var i = startAt
        while (i < 4096) {
            val lx = i shr 8
            val ly = (i shr 4) and 15
            val lz = i and 15
            i++
            if (ly !in ly0..ly1) continue
            val dx = baseX + lx - cx
            val dx2 = dx.toLong() * dx
            if (dx2 > r2) continue
            val dy = baseY + ly - cy
            val dx2dy2 = dx2 + dy.toLong() * dy
            if (dx2dy2 > r2) continue
            val dz = baseZ + lz - cz
            val d2 = dx2dy2 + dz.toLong() * dz
            if (d2 !in innerSq..r2) continue
            lastScanCount++
            pos.set(baseX + lx, baseY + ly, baseZ + lz)
            if (anyBlock) cleanupAnyAt(pos) else vaporiseFluidAt(pos)
            if (lastScanCount >= budget) return i
        }
        return 0
    }

    private fun vaporiseFluidAt(pos: BlockPos.MutableBlockPos) {
        val state = level.getBlockState(pos)
        if (state.isAir || state.fluidState.isEmpty) return
        level.setBlock(pos, AIR_STATE, FLAG_QUIET_NO_SYNC)
        sectionChanges.add(SectionPos.sectionRelativePos(pos))
        passVaporised++
    }

    private fun cleanupAnyAt(pos: BlockPos.MutableBlockPos) {
        val state = level.getBlockState(pos)
        if (state.isAir) return
        @Suppress("DEPRECATION")
        val resistance = state.block.getExplosionResistance()
        if (protectedResistance != null && resistance >= protectedResistance) return
        level.setBlock(pos, AIR_STATE, FLAG_QUIET_NO_SYNC)
        sectionChanges.add(SectionPos.sectionRelativePos(pos))
        passLeftovers++
    }

    private fun endSweepPass() {
        if (sweepDone) return
        val found = passVaporised

        if (found > 0L && sweepPasses < MAX_SWEEP_PASSES) {
            startSweep()
            return
        }

        val falling = countFallingNearSphere()
        if (falling > 0 && sweepPasses < MAX_SWEEP_PASSES) {
            sweepPhase = false
            guardTicksLeft = INFLOW_GUARD_COOLDOWN_TICKS
            return
        }

        verifyRequested = true
        startVerify()
    }

    private fun startVerify() {
        verifyPass++
        passLeftovers = 0L
        verifyPhase = true
        sweepPhase = false
        ready.clear()
        nextShellLow = 0L
        shellExhausted = false
        resumeAt = 0
        stallFrames = 0
    }

    private fun endVerifyPass() {
        if (sweepDone) return
        val found = passLeftovers

        if (found > 0L && verifyPass < MAX_SWEEP_PASSES) {
            startVerify()
            return
        }

        val falling = countFallingNearSphere()
        if (falling > 0 && verifyPass < MAX_SWEEP_PASSES) {
            verifyPhase = false
            guardTicksLeft = INFLOW_GUARD_COOLDOWN_TICKS
            return
        }

        verifyPhase = false
        sweepDone = true
        releaseAllTickets()
    }

    private fun countFallingNearSphere(): Int {
        val box = fallingBox ?: fallingCheckBox().also { fallingBox = it }
        return level.getEntitiesOfClass(FallingBlockEntity::class.java, box).size
    }

    private fun finishSweepGiveUp() {
        if (sweepDone) return
        if (passVaporised > 0L && sweepPasses < MAX_SWEEP_PASSES) {
            startSweep()
            return
        }

        verifyRequested = true
        startVerify()
    }

    private fun finish() {
        if (finished) return
        finished = true

        guardTicksLeft = INFLOW_GUARD_COOLDOWN_TICKS
        level.playSound(
            null,
            centre.x.toDouble(),
            centre.y.toDouble(),
            centre.z.toDouble(),
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.BLOCKS,
            64.0f,
            0.4f
        )
    }

    private fun releaseAllTickets() {
        if (pendingTickets.isEmpty()) return
        val chunkSource = level.chunkSource
        for (pos in pendingTickets.values) {
            chunkSource.removeRegionTicket(TicketType.FORCED, pos, 2, pos)
        }
        pendingTickets.clear()
    }

    fun cancel() {
        releaseAllTickets()

        guardTicksLeft = 0
        sweepPhase = false
        verifyPhase = false
        sweepDone = true
        finished = true
    }

    companion object {
        private const val SHELL_BAND = 4096L

        private const val INFLOW_GUARD_MARGIN = 16

        private const val INFLOW_GUARD_COOLDOWN_TICKS = 40

        private const val FALLING_POLL_INTERVAL = 10

        private const val MAX_GUARD_TAIL_EXTEND = 1200

        private const val FALLING_CHECK_Y_MARGIN = 32

        private const val SWEEP_DEPTH = 32

        private const val MAX_SWEEP_PASSES = 8

        private const val SWEEP_SCAN_BUDGET_FACTOR = 8

        private const val FLAG_CLIENT_ONLY = 2

        private const val UPDATE_KNOWN_SHAPE = 16

        private const val FLAG_QUIET_NO_SYNC = UPDATE_KNOWN_SHAPE

        const val FLAG_QUIET = FLAG_CLIENT_ONLY + UPDATE_KNOWN_SHAPE

        private const val SECTION_REACH = 14L

        private fun shellLimitSq(radius: Int): Long {
            val reach = radius.toLong() + SECTION_REACH
            return reach * reach
        }

        private fun spread21(v: Int): Long {
            var x = (v and 0x1FFFFF).toLong()
            x = (x or (x shl 32)) and 0x1F00000000FFFFL
            x = (x or (x shl 16)) and 0x1F0000FF0000FFL
            x = (x or (x shl 8)) and 0x100F00F00F00F00FL
            x = (x or (x shl 4)) and 0x10C30C30C30C30C3L
            x = (x or (x shl 2)) and 0x1249249249249249L
            return x
        }

        private fun mortonCode(sx: Int, sy: Int, sz: Int): Long = spread21(sx + 512) or
            (spread21(sy + 512) shl 1) or
            (spread21(sz + 512) shl 2)

        private val NEIGHBOUR_OFFSETS = intArrayOf(1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1)

        private val AIR_STATE: BlockState = Blocks.AIR.defaultBlockState()

        private const val MAX_STALL_FRAMES = 1200

        private const val EFFECT_INTERVAL_TICKS = 4

        private fun nearestAxisDistance(lo: Int, hi: Int, c: Int): Long = when {
            c < lo -> (lo - c).toLong()
            c > hi -> (c - hi).toLong()
            else -> 0L
        }

        private fun farthestAxisDistance(lo: Int, hi: Int, c: Int): Long {
            val a = (lo - c).toLong()
            val b = (hi - c).toLong()
            return maxOf(if (a < 0L) -a else a, if (b < 0L) -b else b)
        }

        private fun floorDiv(a: Long, b: Int): Long = Math.floorDiv(a, b.toLong())

        private fun isqrt(v: Long): Long {
            if (v <= 0L) return 0L
            var x = sqrt(v.toDouble()).toLong()
            while (x > 0 && x * x > v) x--
            while ((x + 1) * (x + 1) <= v) x++
            return x
        }

        private fun orderKey(sx: Int, sy: Int, sz: Int): Long = ((sx.toLong() and 0x3FFL) shl 40) or
            ((sz.toLong() and 0x3FFL) shl 30) or
            (((sy + 512).toLong() and 0x3FFL))

        private fun decodeOrderKey(key: Long): Triple<Int, Int, Int> {
            val rawX = ((key ushr 40) and 0x3FFL).toInt()
            val rawZ = ((key ushr 30) and 0x3FFL).toInt()
            val rawY = (key and 0x3FFL).toInt()
            val sx = if (rawX >= 512) rawX - 1024 else rawX
            val sz = if (rawZ >= 512) rawZ - 1024 else rawZ
            val sy = rawY - 512
            return Triple(sx, sy, sz)
        }

        @JvmStatic
        fun removalsForRadius(radius: Int): Int = (radius.toLong() * REMOVALS_PER_RADIUS_UNIT)
            .coerceIn(MIN_REMOVALS_PER_FRAME.toLong(), MAX_REMOVALS_PER_FRAME.toLong())
            .toInt()

        @JvmStatic
        fun chunkWindowForRadius(radius: Int): Int = (radius / 16).coerceIn(MIN_CHUNK_WINDOW, MAX_CHUNK_WINDOW)

        @JvmStatic
        fun sectionsForRadius(radius: Int): Int = (radius * 2).coerceIn(MIN_SECTIONS_PER_FRAME, MAX_SECTIONS_PER_FRAME)

        private const val REMOVALS_PER_RADIUS_UNIT = 64L
        private const val MIN_REMOVALS_PER_FRAME = 1024
        private const val MAX_REMOVALS_PER_FRAME = 32768

        private const val MIN_CHUNK_WINDOW = 64
        private const val MAX_CHUNK_WINDOW = 128

        private const val MIN_SECTIONS_PER_FRAME = 512
        private const val MAX_SECTIONS_PER_FRAME = 4096
    }
}
