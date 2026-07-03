---
navigation:
  title: 创造之门与创造聚合仪的相关修改
  parent: modify/modify_index.md
  position: 7
item_ids:
  - gtceu:door_of_create
  - gtceu:create_aggregation
---

# 创造之门与创造聚合仪的相关修改

<Row>
    <BlockImage id = "gtceu:door_of_create" scale = "4" />

    <BlockImage id = "gtceu:create_aggregation" scale = "4" />
</Row>

* 可以通过安装 <ItemLink id="gtladditions:me_block_conservation" /> 来转换
* 在输入总线中选择24号编程电路即可运行转换
* 只能在主机中放入**极限转换卡**, 放入后获得并行加成, 未放入同样无法正常工作
* 创造之门: 65536并行; 创造聚合仪: 8192并行
* 需要手持 <ItemLink id="gtceu:creative_data_access_hatch" /> 右键**嬗变总线**安装才能正常使用
* 有关**嬗变总线**其余使用方法与 <ItemLink id="gtladditions:atomic_transmutation_core" /> 类似
* 输入总线中选择1号编程电路则为原版方式

### 创造之门转换配方
<Row>
    <Recipe id="gtladditions:transmutation_block_conversion/tagprefix.block" />

    <Recipe id="gtladditions:transmutation_block_conversion/block.minecraft.command_block" />
</Row>

### 创造聚合仪转换配方
<Row>
    <Recipe id="gtladditions:transmutation_block_conversion/block.minecraft.chain_command_block" />

    <Recipe id="gtladditions:transmutation_block_conversion/block.minecraft.repeating_command_block" />
</Row>
