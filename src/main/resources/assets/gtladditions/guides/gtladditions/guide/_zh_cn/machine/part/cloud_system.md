---
navigation:
  title: 云端算力/研究数据系统
  icon: gtmthings:iv_huge_output_dual_hatch
  parent: part/machine_part_index.md
  position: 10
item_ids:
  - gtladditions:cloud_data_hatch
  - gtladditions:cloud_data_machine
  - gtladditions:cloud_computation_monitor
  - gtladditions:cloud_computation_transmitter_hatch
  - gtladditions:cloud_computation_receiver_hatch
  - gtceu:research_station
---

# 云端算力/研究数据系统

<Row>
    <BlockImage id="gtladditions:cloud_data_hatch" scale="3" />

    <BlockImage id="gtladditions:cloud_data_machine" scale="3" />

    <BlockImage id="gtladditions:cloud_computation_monitor" scale="3" />

    <BlockImage id="gtladditions:cloud_computation_transmitter_hatch" scale="3" />

    <BlockImage id="gtladditions:cloud_computation_receiver_hatch" scale="3" />
</Row>

* 需要在<Color color="#55FF55">**UEV**</Color>阶段才能制作它
* 云端算力供应仓等价于<ItemLink id="gtmthings:wireless_computation_transmitter_hatch" />，云端算力请求仓等价于<ItemLink id="gtmthings:wireless_computation_receiver_hatch" />，云端算力监控器负责监控云端算力系统的算力IO情况
* 只有带有桥接功能的多方块机器才能为云端算力系统提供算力；在云端算力监控器高亮坐标后若机器位于玩家同一维度则会关闭GUI并将玩家视角指向机器，若位于不同维度则在聊天栏给出可传送的坐标
* <ItemLink id="gtceu:network_switch" />*不能放置*云端算力供应/请求仓
* 云端研究数据请求仓请求仓等价于<ItemLink id="gtceu:wireless_data_receiver_hatch" />
* 云端研究数据存储器负责将数据模块/数据球/闪存存储至云端研究数据系统，每个数据模块/数据球/闪存增加393,216EU/t的能耗，当放入<ItemLink id="gtceu:creative_data_access_hatch" />时可为所有请求仓提供所有研究数据和锁定能耗为393,216EU/t（拆除存储器时创造模式数据访问仓*不会*掉落）
* 使用闪存绑定后，在<ItemLink id="gtceu:research_station" />完成研究时会自动尝试将研究数据上传至可用的云端研究数据存储器
* 云端算力/研究数据系统均可以*跨维度*工作
