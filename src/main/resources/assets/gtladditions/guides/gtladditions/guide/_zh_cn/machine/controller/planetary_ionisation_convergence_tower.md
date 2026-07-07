---
navigation:
  title: 行星电离汇流塔
  icon: planetary_ionisation_convergence_tower
  parent: controller/multiblock_controller.md
  position: 10
item_ids:
  - planetary_ionisation_convergence_tower
---

# 行星电离汇流塔

<BlockImage id = "planetary_ionisation_convergence_tower" scale = "8"/>

* 只能使用泰坦钢及以上等级的线圈
* 每3s为一次工作周期
* 如果内部能量缓存已满后仍然有能量进入将会以机器为中心产生一次极大规模的爆炸
* 在工作周期开始时会产生一次瞬时极高功率EU脉冲（1tick）至内部能量缓存中，然后在剩余时间内以较低功率平滑放电至内部能量缓存中
* 在脉冲结束后内部能量缓存会通过动力仓/激光源仓向外界输出电量
* 恒星热力容器等级影响内部能量缓存量
> 基础：54,120,000,000,000EU \
> 高级：3,475,000,000,000,000EU\
> 终极：1,160,000,000,000,000,000EU
* 线圈等级影响消耗的流体种类和一个周期内的消耗量以及发电量（发电数据为同组最高级线圈，每降一级瞬时和放电数值均除以16）
> 泰坦钢至精金：<FluidLink id="gtceu:rhenium" /> 73728mB，<FluidLink id="gtceu:ice" /> 8KB，<ItemLink id="kubejs:space_drone_mk2" /> 2x10^(-4)个 \
> 瞬时-4096A MAX 放电-16A MAX\
> 超能硅岩-塔兰至星辉：<FluidLink id="gtceu:promethium" /> 36864mB，<FluidLink id="gtceu:liquid_helium" /> 4KB，<ItemLink id="kubejs:space_drone_mk4" /> 1x10^(-4)个 \
> 瞬时-524288A MAX 放电-256A MAX\
> 无尽至永恒：<FluidLink id="gtceu:crystalmatrix" /> 9216mB，<FluidLink id="kubejs:gelid_cryotheum" /> 1KB，<ItemLink id="kubejs:space_drone_mk6" /> 2.5x10^(-5)个 \
> 瞬时-268435456A MAX 放电-131072A MAX
* 通过手持<ItemLink id="gtmthings:creative_laser_hatch"/>右键塞入一个到机器中, 开启特殊超频模式
> 工作周期缩短至1s，所有能量均直接输出至无线电网且最大单次输出电流提升到64A MAX+16，无视线圈等级和内部缓存
* 此时的材料消耗量和种类为
> <FluidLink id="gtceu:miracle" /> 10mB，<ItemLink id="kubejs:hyperdimensional_drone" /> 1x10^(-6)个
* **注意**: 若将主机拆除后特殊超频模式会重置且不会返还创造模式激光靶仓