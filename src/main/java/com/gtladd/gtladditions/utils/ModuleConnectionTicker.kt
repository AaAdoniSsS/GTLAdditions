package com.gtladd.gtladditions.utils

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineHost
import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.TickableSubscription

import java.util.WeakHashMap

object ModuleConnectionTicker {

    private const val INTERVAL_TICKS = 20

    private val subscriptions = WeakHashMap<MetaMachine, TickableSubscription>()

    fun <H : IModularMachineHost<H>, M : IModularMachineModule<H, M>> attach(machine: MetaMachine, module: M) {
        if (subscriptions.containsKey(machine)) return
        subscriptions[machine] = machine.subscribeServerTick {
            if (machine.offsetTimer % INTERVAL_TICKS != 0L) return@subscribeServerTick
            if (!module.isFormed) {
                stop(machine)
                return@subscribeServerTick
            }
            val host = module.getHost()
            if (host != null && host.isFormed) return@subscribeServerTick
            if (!module.findAndConnectToHost()) module.removeFromHost(module.getHost())
        }
    }

    fun stop(machine: MetaMachine) {
        subscriptions.remove(machine)?.unsubscribe()
    }
}
