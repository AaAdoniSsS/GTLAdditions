---
navigation:
  title: 递归反演锻炉
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

# 递归反演锻炉

<BlockImage id = "recursive_reverse_forge" scale = "8"/>

* 耗能倍数: 0.8
* 最大并行数: 2,147,483,647
* 拥有复合配方类型: 超维度熔炼, 恒星热能熔炼
* 拥有无损超频, 且超频次数不限
* 只能使用激光仓
* 可以安装模块来获得额外加成

### 逆时助推引擎

<BlockImage id = "reverse_time_boosting_engine" scale = "4"/>

* **只能安装在递归反演锻炉结构上, 且每个递归反演锻炉只能安装一个该模块**

> 安装该模块后, 可以减少配方耗时, 具体倍数为: \
> <Latex math = "\min{(0.8, 0.05+0.8*e^{-3.2*投入比例^{1.9}})}" /> \
> 投入比例为: 投入量乘以2/(初始配方输出量*当前温度), 投入比例不会大于1, 投入比例小于10%时不会激活耗时减免功能(初始配方输出量指1并行无额外加成的配方产出) \
> 只需投入一种输出物品或流体即可, 不能使用<FluidLink id="gtceu:dimensionallytranscendentresidue" />,<ItemLink id="kubejs:extremely_durable_plasma_cell" />,<ItemLink id="kubejs:time_dilation_containment_unit" />, <ItemLink id="kubejs:plasma_containment_cell" /> \
> 初始温度为48000K, 若机器处于工作状态则温度会以65K/t的速度增长, 处于非工作状态则温度会以900K/s的速度减少(不低于48000K) \
> 可以通入不同的流体来控制温度,可用加热介质为<FluidLink id="minecraft:lava" />2500K,<FluidLink id="gtceu:blaze" />4600K,<FluidLink id="gtceu:raw_star_matter_plasma" />14000K, 可用冷却液为<FluidLink id="gtceu:ice" />1900K,<FluidLink id="gtceu:liquid_helium" />3400K,<FluidLink id="kubejs:gelid_cryotheum" />6700K \
> 加热介质和冷却液消耗速度均为100B/1s \
> 投入的配方产物会在配方处理结束后按比例返还 \
> 当温度位于[93000K, 97000K]时返还比例为100% \
> 当温度低于区间下限时返还比例为:  \
> <Latex math = "0.5+0.5*\frac{当前温度-48000}{45000}" /> \
> 当温度超过区间上限时返还比例为: \
> <Latex math = "1-0.85*(\frac{当前温度-97000}{13000})^{0.42}" /> \
> 当温度超过105000K时将触发过热保护机制, 默认以7125K/s的冷却速度降低温度(使用冷却剂可以额外增加降温速度, 实际降温速度为默认速度加冷却剂降温速度), 直到降低到48000K前机器本体将不会处理配方 \
> 可以安装<ItemLink id="gtladditions:vientiane_transcription_node" />来控制温度，类似于中子传感器 \
> **只能**使用巨型输入仓提供控温流体, **只能**使用<ItemLink id="super_input_dual_hatch" />提供投入的配方产物

### 催化迭升阵列

<BlockImage id = "catalytic_cascade_array" scale = "4"/>

* **只能安装在递归反演锻炉结构上, 且每个递归反演锻炉只能安装一个该模块**

> 安装该模块后, 可以使配方耗能再乘以0.15, 且可以让配方双倍产出 \
> 拥有30秒的催化剂**循环周期**. 在每次催化剂循环周期开始时, 模块将会随机输出1-15的红石信号 \
> 需要通过<ItemLink id="gtladditions:vientiane_transcription_node" />来接收输出红石信号 \
> **只能**使用LV巨型输入仓来接收输入的催化剂 \
> 若红石信号为1-3则代表本次周期使用催化剂为<FluidLink id="gtceu:dimensionallytranscendentcrudecatalyst" />, 以此类推, 往后顺序为<FluidLink id="gtceu:dimensionallytranscendentprosaiccatalyst" />,<FluidLink id="gtceu:dimensionallytranscendentresplendentcatalyst" />, <FluidLink id="gtceu:dimensionallytranscendentexoticcatalyst" />, <FluidLink id="gtceu:dimensionallytranscendentstellarcatalyst" /> \
> 在循环开始0-5s内模块不会工作, 5s后模块将按照输出的红石信号消耗催化剂, 催化剂消耗速度为40B/1s, 效果将生效至下次循环第五秒 \
> 若输入错误的催化剂则本次循环剩余时间不会有增产效果且后续输入的任何催化剂都将被消耗, 能耗减免效果保留 \
> 当前及后续处理带有<ItemLink id="kubejs:extremely_durable_plasma_cell" />,<ItemLink id="kubejs:time_dilation_containment_unit" />,<ItemLink id="kubejs:plasma_containment_cell" />的配方不会参与翻倍过程 \
> **磁流聚敛核心模块优先级高于该模块**

### 磁流聚敛核心

<BlockImage id = "magnetorheological_convergence_core" scale = "4"/>

* **只能安装在递归反演锻炉结构上, 且每个递归反演锻炉只能安装一个该模块**

> 安装该模块后, 可以使配方产出聚焦在第一个物品和流体上，将第二个产物产出转换为第一个产物的额外产出 \
> 拥有12s的燃料输入周期, 且**只能**使用两个ulv巨型输入总线和一个lv巨型输入仓来接收燃料 \
> 模块第一次启动时会在<ItemLink id="kubejs:black_body_naquadria_supersolid" />,<ItemLink id="kubejs:quantum_anomaly" />,<ItemLink id="kubejs:hyper_stable_self_healing_adhesive" />,<FluidLink id="gtceu:exciteddtec" />,<FluidLink id="gtceu:exciteddtsc" />中随机选择两种物品和一种流体作为维持模块运行的燃料 \
> 需要精确输入被模块选中的燃料以激活聚焦功能，若输入数量错误会在模块GUI中提示 \
> 同时固定消耗2/s的磁物质块, 若输入不足则无法聚焦配方输出 \
> 当前及后续处理带有<ItemLink id="kubejs:extremely_durable_plasma_cell" />,<ItemLink id="kubejs:time_dilation_containment_unit" />, <ItemLink id="kubejs:plasma_containment_cell" />的配方不会参与聚焦过程 \
> **逆时助推引擎模块优先级高于该模块**

### 超维聚能元件

<BlockImage id = "hyperdimensional_energy_concentrator" scale = "4"/>

* **只能安装在递归反演锻炉结构上, 且每个递归反演锻炉只能安装一个该模块**

> 安装该模块后, 可以使递归反演锻炉直接从无线电网中获取能量来工作 \
> 需要在该模块主机中放入 <ItemLink id="kubejs:hyperdimensional_drone" />, 每放入一个可以使获取的功率上限加16A<Color color="#FF0000">**M**</Color><Color color="#00FF00">**A**</Color><Color color="#0000FF">**X**</Color><Color color="#FFFF00">**+**</Color><Color color="#FF0000">**16**</Color>. \
> 基础可获得最大功率为1A MAX, 需要放入超维度无人机才能增加获得功率上限（同时会将配方等级锁定为MAX）, 最多可以放入64个 \
> 每小时都会发射(消耗)一台超维度无人机以维持与无线电网的连接 \
> 需要提供524288 CWU/t, 64A<Color color="#FF0000">**MAX**</Color>以及240B/s的极寒之凛冰来维持模块运行

### 分形操纵机关

<BlockImage id = "fractal_manipulator" scale = "4"/>

* **只能安装在递归反演锻炉结构上, 且每个递归反演锻炉最多只能安装四个该模块**

> 耗能倍数: 0.8 \
> 最大并行数: 2,147,483,647 \
> 拥有无损超频, 且超频次数不限 \
> 只能使用激光仓 \
> 可以与递归反演锻炉共享催化迭升阵列以及超维聚能元件的加成 \
> **当且仅当**逆时助推引擎模块运行正常时该模块可以正常工作 \
> 模块运行配方时需额外输入一份配方对应的**触媒**(通过JEI查看), 触媒**不参与**并行计算, 配方运行结束后**返还**触媒


