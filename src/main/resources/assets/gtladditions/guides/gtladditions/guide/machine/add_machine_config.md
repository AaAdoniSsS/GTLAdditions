---
navigation:
  title: GTLAdditions Machine Config
  parent: machine/machine_index.md
  position: 4
---

# GTLAdditions Machine Config

The default number of threads for most GTLAdditions machines is 128 (where applicable; for reference, the original cross-recipe parallel machine uses 64 threads).

## **Equally Divided Mode**

> In Equally Divided Mode, the machine will process all possible recipes, with parallel recipes distributed as evenly as possible \
> If the total recipe time after equal distribution exceeds 1,000 seconds, the number of recipes run in parallel will not be distributed equally; instead, only one recipe that can be processed will be run \
> In this mode, the total parallel number is: thread count × machine parallel count
> This mode does not support batch processing

## **Extreme Mode**

> In Extreme Mode, the machine will concentrate on processing a portion of recipes, featuring lossless overclocking and 1toc (For concepts of lossless overclocking and 1toc, please refer to the task book)\
> Under this mode, the bonus from the Configurable Maintenance will not take effect \
> In this mode, the number of threads is the maximum number of recipes that can be processed \
> This mode automatically enables batch processing and cannot be disabled \
> Unless otherwise specified or provided for by a specific mechanism, the operation of GTLAdditions machines that are not cross-recipe parallel machines also follows the same mechanism as in extreme mode

* You can select the default mode in the mod configuration. The initial default mode is the **Equally Divided Mode**.

#### **Limit Duration Configuration for Recipe**

> The button can be opened in the lower left corner of the GUI of some GTLAdditions machines to adjust the initial default value. \
> The initial default value is 1 second. It ranges from 5 tick (minimum) to 10 seconds (maximum). \
> The default value can also be modified in the mod configuration file.
