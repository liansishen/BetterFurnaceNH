# Modernity 适配材质包 / Modernity resource pack

`Modernity-BetterFurnaceNH/` 保存完整适配包，覆盖 BetterFurnaceNH 熔炉界面的四个漏斗输入/输出开关材质。适用于 Minecraft 1.7.10，`pack_format` 为 `1`。

## 安装

1. 从模组的 GitHub Release 下载 `Modernity-BetterFurnaceNH-<版本>.zip`，放入游戏的 `resourcepacks/`。
2. 在资源包界面启用 Modernity 和本适配包，将本适配包放在 Modernity 上方。
3. 编辑材质后按 F3+T 重载。也可直接将 `Modernity-BetterFurnaceNH/` 目录复制到游戏 `resourcepacks/` 后启用。

## 打包与发布

运行 `./gradlew packageResourcePacks`（Windows 使用 `./gradlew.bat`），输出为 `build/resourcepacks/Modernity-BetterFurnaceNH-<版本>.zip`。`build` 与 `assemble` 也会执行打包。

默认使用项目版本；可通过 `-PresourcePackVersion=<版本>` 指定文件名中的版本。ZIP 根目录包含 `pack.mcmeta`、`pack.png`、`assets/` 和包内说明，支持直接安装。归档采用固定时间戳和稳定文件顺序。

[发布流程](../CONTRIBUTING.md)：创建 PR，等待 CI 成功，合并 PR，等待主分支 CI 成功，再推送版本标签。标签工作流将三个 JAR 和 Modernity ZIP 发布到同一个 Release，ZIP 版本与标签一致。

## 材质规范

- 路径：`assets/betterfurnacenh/textures/gui/buttons/`。
- 四个文件：`input_on.png`、`input_off.png`、`output_on.png`、`output_off.png`。
- 现有按钮原图为 18×18 RGBA PNG，模组按 16×16 绘制，点击区域为 16×16。材质保存为 RGB/RGBA。
- 封面为 128×128 RGBA，与 BackpackEnhance 适配包使用相同外框、配色和中央按钮，中央字母为 `F`。
- 材质与封面的来源、改编和许可见 [包内说明](Modernity-BetterFurnaceNH/README.md)。

## English

Download the Modernity ZIP from the mod's release into the game's `resourcepacks/` directory, enable it above Modernity and reload with F3+T after editing. The source directory is also directly installable. Run `packageResourcePacks` to build the ZIP; `build` and `assemble` include it. Use `-PresourcePackVersion=<version>` to override its filename version. Tagged releases publish all three JARs and the matching Modernity ZIP together after the PR and master CI checks pass.
