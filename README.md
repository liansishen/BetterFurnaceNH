# Better Furnace NH / 更好的熔炉 NH

An addon mod for GTNH (GregTech: New Horizons) that adds tiered furnaces with fluid fuel support and blast furnace upgrades.

GTNH 的附属模组，添加了支持流体燃料的分级熔炉和高炉。

## Features / 特性

- **Iron Furnace / 铁熔炉** — 1.5× faster than vanilla furnace / 速度是原版熔炉的 1.5 倍
- **Gold Furnace / 金熔炉** — 1.5× faster than Iron Furnace (2.25× total) / 速度是铁熔炉的 1.5 倍（总计 2.25 倍）
- **Diamond Furnace / 钻石熔炉** — 1.5× faster than Gold Furnace (3.375× total) / 速度是金熔炉的 1.5 倍（总计 3.375 倍）
- **Fluid Fuel / 流体燃料** — Lava and Creosote can be used as fuel via internal fluid tank / 岩浆和杂酚油可通过内部流体槽作为燃料
- **Blast Furnace Upgrades / 高炉升级** — Iron/Gold/Diamond blast furnaces (requires Et-Futurum-Requiem), run at 2× speed / 铁/金/钻石高炉（需要 Et-Futurum-Requiem），速度为同级熔炉 2 倍
- **Configurable / 可配置** — speed, tank capacity, and burn values adjustable via GUI / 速度、容量、燃烧值均可通过 GUI 调整
- **i18n / 国际化** — English and Simplified Chinese / 英文和简体中文

## Fluid Fuel / 流体燃料

Right-click with a Lava/Creosote bucket or pipe in with IFluidHandler. Fluid is consumed every 20 ticks while running.

手持岩浆/杂酚油桶右键熔炉，或用管道输入。运行时每 20 tick 消耗一次。

| Fluid / 流体 | Burn Time / 燃烧时间 (per 1000L) | Items per 1000L / 每 1000L 烧炼 |
|---|---|---|
| Lava / 岩浆 | 20000 ticks | 100 |
| Creosote / 杂酚油 | 6400 ticks | 32 |

Tank capacity default 8000L, max 32000L. / 流体槽默认 8000L，最大 32000L。

## Configuration / 配置

Config file: `config/betterfurnacenh.cfg` or Mods → Better Furnace NH → Config.

| Setting / 设置 | Default / 默认 | Range / 范围 |
|---|---|---|
| Iron Speed / 铁熔炉倍率 | 1.5 | 0.1–100 |
| Gold Speed / 金熔炉倍率 | 1.5 | 0.1–100 |
| Diamond Speed / 钻石熔炉倍率 | 1.5 | 0.1–100 |
| Tank Capacity / 流体槽容量 | 8000 L | 1000–32000 |
| Lava Burn/Bucket / 岩浆燃烧 | 20000 tick | 1–1000000 |
| Creosote Burn/Bucket / 杂酚油燃烧 | 6400 tick | 1–1000000 |

## License / 许可证

MIT
