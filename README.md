# Better Furnace NH / 更好的熔炉 NH

An addon mod for GTNH (GregTech: New Horizons) that adds tiered furnaces with fluid fuel support and blast furnace upgrades.

GTNH 的附属模组，添加了支持流体燃料的分级熔炉和高炉。

## Features / 特性

- **Iron Furnace / 铁熔炉** — 1.5× faster than vanilla furnace / 速度是原版熔炉的 1.5 倍
- **Gold Furnace / 金熔炉** — 1.5× faster than Iron Furnace (2.25× total) / 速度是铁熔炉的 1.5 倍（总计 2.25 倍）
- **Diamond Furnace / 钻石熔炉** — 1.5× faster than Gold Furnace (3.375× total) / 速度是金熔炉的 1.5 倍（总计 3.375 倍）
- **Fluid Fuel / 流体燃料** — Lava and Creosote can be used as fuel via internal fluid tank / 岩浆和杂酚油可通过内部流体槽作为燃料
- **Blast Furnace Upgrades / 高炉升级** — Iron/Gold/Diamond blast furnaces (requires Et-Futurum-Requiem), run at 2× speed / 铁/金/钻石高炉（需要 Et-Futurum-Requiem），速度为同级熔炉 2 倍
- **Hopper Upgrade / 漏斗升级组件** — Install a Hopper Upgrade on the top or bottom of a furnace to enable auto item input/output; toggle on/off via GUI buttons / 在熔炉顶部或底部安装漏斗升级组件以启用自动物品输入/输出，可通过 GUI 按钮开关
- **Configurable / 可配置** — speed, tank capacity, and burn values adjustable via GUI / 速度、容量、燃烧值均可通过 GUI 调整
- **i18n / 国际化** — English and Simplified Chinese / 英文和简体中文

## Hopper Upgrade / 漏斗升级组件

Craft a Hopper Upgrade from a vanilla Hopper (shapeless recipe). Sneak-right-click the top or bottom face of a furnace to install it.

用一个原版漏斗无序合成漏斗升级组件。潜行右键熔炉顶面或底面即可安装。

| Face / 面 | Function / 功能 |
|---|---|
| Top / 顶部 | Auto Input — pulls smeltable items from the inventory above into the input slot / 自动输入 — 从上方容器抽取可烧炼物品进入输入槽 |
| Bottom / 底部 | Auto Output — pushes smelted results from the output slot into the inventory below / 自动输出 — 从输出槽将成品推入下方容器 |

- **Install / 安装**: Sneak-right-click with the Hopper Upgrade on the top or bottom face / 潜行右键顶面或底面安装
- **Remove / 拆除**: Right-click the installed face with a GT crowbar / 用 GT 撬棍右键已安装的面拆除（返还升级组件）
- **Toggle / 开关**: GUI buttons appear when installed; click to toggle on/off / 安装后 GUI 出现按钮，点击可开关
- **Grid preview / 九宫格预览**: Hold the Hopper Upgrade or a crowbar and aim at a furnace to see a GT-style 3×3 grid; installable faces (top/bottom) are blank, others are marked with X / 手持漏斗升级组件或撬棍瞄准熔炉时显示 GT 风格九宫格，可安装面（顶/底）留空，不可安装面标记 X

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
| Hopper Transfer Rate / 漏斗传输间隔 | 8 tick | 1–200 |
| Hopper Items/Transfer / 每次传输数量 | 1 | 1–64 |

## License / 许可证

MIT
